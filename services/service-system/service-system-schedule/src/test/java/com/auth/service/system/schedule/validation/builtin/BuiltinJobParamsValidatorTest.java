package com.auth.service.system.schedule.validation.builtin;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.task.builtin.FeignInvokeJob;
import com.auth.service.system.schedule.task.builtin.HttpInvokeJob;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import com.auth.service.system.schedule.validation.task.BuiltinJobParamsValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * {@link BuiltinJobParamsValidator} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("BuiltinJobParamsValidator 内置 job_params 校验")
@ExtendWith(MockitoExtension.class)
class BuiltinJobParamsValidatorTest {

	private static final Validator BEAN_VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Mock
	private BuiltinJobParamsParser builtinJobParamsParser;

	private BuiltinJobParamsValidator validator;

	@BeforeEach
	void setUp() {
		validator = new BuiltinJobParamsValidator(builtinJobParamsParser, BEAN_VALIDATOR);
	}

	@Test
	@DisplayName("validateIfPresent：匹配内置类时应委派 parse")
	void validateIfPresent_whenBuiltinClass_delegatesParser() {
		String json = "{\"method\":\"GET\",\"url\":\"https://example.com\"}";
		HttpInvokeJobParams params = new HttpInvokeJobParams();
		params.setMethod(HttpMethod.GET);
		params.setUrl("https://example.com");
		when(builtinJobParamsParser.parse(json, HttpInvokeJobParams.class)).thenReturn(params);

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams(json);

		validator.validateIfPresent(job);

		verify(builtinJobParamsParser).parse(json, HttpInvokeJobParams.class);
	}

	@Test
	@DisplayName("validateIfPresent：非内置类时应跳过")
	void validateIfPresent_whenUnknownClass_skips() {
		SysJobEntity job = new SysJobEntity();
		job.setJobClass("com.example.OtherJob");
		job.setJobParams("{\"any\":true}");

		validator.validateIfPresent(job);

		verify(builtinJobParamsParser, never()).parse(anyString(), any());
	}

	@Test
	@DisplayName("validateIfPresent：parse 失败时应抛出 SysJobException")
	void validateIfPresent_whenParseFails_propagates() {
		String json = "bad";
		when(builtinJobParamsParser.parse(json, HttpInvokeJobParams.class)).thenThrow(new SysJobException(
				com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CONFIG_INVALID, "job_params", "bad"));

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams(json);

		assertThatThrownBy(() -> validator.validateIfPresent(job)).isInstanceOf(SysJobException.class);
	}

	@Test
	@DisplayName("validateIfPresent：FeignInvoke 内置类应使用对应载荷类型")
	void validateIfPresent_whenFeignInvokeClass_usesFeignParamsType() {
		String json = "{\"serviceName\":\"order-service\",\"path\":\"/api\",\"method\":\"GET\"}";
		FeignInvokeJobParams params = new FeignInvokeJobParams();
		params.setServiceName("order-service");
		params.setPath("/api");
		params.setMethod(com.auth.service.system.schedule.model.jobparams.BuiltinRemoteHttpMethod.GET);
		when(builtinJobParamsParser.parse(json, FeignInvokeJobParams.class)).thenReturn(params);

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(FeignInvokeJob.class.getName());
		job.setJobParams(json);

		validator.validateIfPresent(job);

		verify(builtinJobParamsParser).parse(json, FeignInvokeJobParams.class);
	}

	@Test
	@DisplayName("集成：合法 HttpInvoke job_params 应通过 Bean Validation")
	void integration_whenValidHttpParams_passes() {
		BuiltinJobParamsValidator integrationValidator = createIntegrationValidator();

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams("{\"method\":\"GET\",\"url\":\"https://example.com\"}");

		assertThatCode(() -> integrationValidator.validateIfPresent(job)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("集成：HttpInvoke JSON 对象 body 应通过校验")
	void integration_whenHttpParamsWithJsonObjectBody_passes() {
		BuiltinJobParamsValidator integrationValidator = createIntegrationValidator();

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams(
				"{\"method\":\"POST\",\"url\":\"https://example.com\",\"body\":{\"title\":\"foo\",\"userId\":1}}");

		assertThatCode(() -> integrationValidator.validateIfPresent(job)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("集成：query 数值应反序列化为 Map<String,String>")
	void integration_whenQueryScalars_passes() {
		BuiltinJobParamsValidator integrationValidator = createIntegrationValidator();

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams(
				"{\"method\":\"GET\",\"url\":\"https://example.com\",\"query\":{\"page\":\"1\",\"userId\":2}}");

		assertThatCode(() -> integrationValidator.validateIfPresent(job)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("集成：嵌套 query 应抛出 SysJobException")
	void integration_whenQueryNested_throws() {
		BuiltinJobParamsValidator integrationValidator = createIntegrationValidator();

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams("{\"method\":\"GET\",\"url\":\"https://example.com\",\"query\":{\"filter\":{\"id\":1}}}");

		assertThatThrownBy(() -> integrationValidator.validateIfPresent(job)).isInstanceOf(SysJobException.class);
	}

	@Test
	@DisplayName("集成：缺少 url 应抛出 SysJobException")
	void integration_whenUrlMissing_throws() {
		BuiltinJobParamsValidator integrationValidator = createIntegrationValidator();

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams("{\"method\":\"GET\"}");

		assertThatThrownBy(() -> integrationValidator.validateIfPresent(job)).isInstanceOf(SysJobException.class);
	}

	@Test
	@DisplayName("集成：空 job_params 应抛出 SysJobException")
	void integration_whenBlankJobParams_throws() {
		BuiltinJobParamsValidator integrationValidator = createIntegrationValidator();

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams(" ");

		assertThatThrownBy(() -> integrationValidator.validateIfPresent(job)).isInstanceOf(SysJobException.class);
	}

	private static BuiltinJobParamsValidator createIntegrationValidator() {
		return new BuiltinJobParamsValidator(new BuiltinJobParamsParser(new ObjectMapper()), BEAN_VALIDATOR);
	}

}
