package com.auth.service.system.schedule.validation.builtin;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.support.task.BuiltinJobParamsParser;
import com.auth.service.system.schedule.task.builtin.FeignInvokeJob;
import com.auth.service.system.schedule.task.builtin.HttpInvokeJob;
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

	@Mock
	private BuiltinJobParamsParser builtinJobParamsParser;

	private BuiltinJobParamsValidator validator;

	@BeforeEach
	void setUp() {
		validator = new BuiltinJobParamsValidator(builtinJobParamsParser);
	}

	@Test
	@DisplayName("validateIfPresent：匹配内置类时应委派解析校验")
	void validateIfPresent_whenBuiltinClass_delegatesParser() {
		String json = "{\"version\":1,\"method\":\"GET\",\"url\":\"https://example.com\"}";
		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams(json);

		validator.validateIfPresent(job);

		verify(builtinJobParamsParser).parseAndValidate(json, HttpInvokeJobParams.class);
	}

	@Test
	@DisplayName("validateIfPresent：非内置类时应跳过")
	void validateIfPresent_whenUnknownClass_skips() {
		SysJobEntity job = new SysJobEntity();
		job.setJobClass("com.example.OtherJob");
		job.setJobParams("{\"any\":true}");

		validator.validateIfPresent(job);

		verify(builtinJobParamsParser, never()).parseAndValidate(anyString(), any());
	}

	@Test
	@DisplayName("validateIfPresent：内置类参数非法时应抛出 SysJobException")
	void validateIfPresent_whenParamsInvalid_propagates() {
		String json = "bad";
		when(builtinJobParamsParser.parseAndValidate(json, HttpInvokeJobParams.class)).thenThrow(new SysJobException(
				com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_PARAMS_JSON_INVALID, "bad"));

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams(json);

		assertThatThrownBy(() -> validator.validateIfPresent(job)).isInstanceOf(SysJobException.class);
	}

	@Test
	@DisplayName("validateIfPresent：FeignInvoke 内置类应使用对应载荷类型")
	void validateIfPresent_whenFeignInvokeClass_usesFeignParamsType() {
		String json = "{\"version\":1,\"serviceName\":\"order-service\",\"path\":\"/api\",\"method\":\"GET\"}";
		SysJobEntity job = new SysJobEntity();
		job.setJobClass(FeignInvokeJob.class.getName());
		job.setJobParams(json);

		validator.validateIfPresent(job);

		verify(builtinJobParamsParser).parseAndValidate(json,
				com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams.class);
	}

	@Test
	@DisplayName("集成：合法 HttpInvoke job_params 应通过 Bean Validation")
	void integration_whenValidHttpParams_passes() {
		Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
		BuiltinJobParamsParser parser = new BuiltinJobParamsParser(new ObjectMapper(), beanValidator);
		BuiltinJobParamsValidator integrationValidator = new BuiltinJobParamsValidator(parser);

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams("{\"version\":1,\"method\":\"GET\",\"url\":\"https://example.com\"}");

		assertThatCode(() -> integrationValidator.validateIfPresent(job)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("集成：缺少 url 应抛出 SysJobException")
	void integration_whenUrlMissing_throws() {
		Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
		BuiltinJobParamsParser parser = new BuiltinJobParamsParser(new ObjectMapper(), beanValidator);
		BuiltinJobParamsValidator integrationValidator = new BuiltinJobParamsValidator(parser);

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpInvokeJob.class.getName());
		job.setJobParams("{\"version\":1,\"method\":\"GET\"}");

		assertThatThrownBy(() -> integrationValidator.validateIfPresent(job)).isInstanceOf(SysJobException.class);
	}

}
