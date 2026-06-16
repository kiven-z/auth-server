package com.auth.service.system.schedule.task.support;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.model.jobparams.VersionedJobParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link BuiltinJobParamsParser} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("内置任务 job_params 解析")
class BuiltinJobParamsParserTest {

	private BuiltinJobParamsParser parser;

	@BeforeEach
	void setUp() {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		parser = new BuiltinJobParamsParser(new ObjectMapper(), validator);
	}

	@Test
	@DisplayName("parseAndValidate：合法 JSON 应反序列化并通过校验")
	void parseAndValidate_whenJsonValid_returnsParams() {
		SampleParams actual = parser.parseAndValidate("{\"version\":1,\"name\":\"demo\"}", SampleParams.class);

		assertThat(actual.getVersion()).isEqualTo(1);
		assertThat(actual.getName()).isEqualTo("demo");
	}

	@Test
	@DisplayName("parseAndValidate：缺少必填字段应抛出 SysJobException")
	void parseAndValidate_whenBeanValidationFails_throws() {
		assertThatThrownBy(() -> parser.parseAndValidate("{\"version\":1}", SampleParams.class))
			.isInstanceOf(SysJobException.class)
			.hasMessageContaining("must not be blank");
	}

	@Test
	@DisplayName("parseAndValidate：不支持的版本号应抛出 SysJobException")
	void parseAndValidate_whenVersionUnsupported_throws() {
		assertThatThrownBy(() -> parser.parseAndValidate("{\"version\":2,\"name\":\"demo\"}", SampleParams.class))
			.isInstanceOf(SysJobException.class);
	}

	@Test
	@DisplayName("parseAndValidate：HttpInvokeJobParams method 应反序列化为 HttpMethod")
	void parseAndValidate_whenHttpInvokeParams_deserializesHttpMethod() {
		HttpInvokeJobParams actual = parser.parseAndValidate(
				"{\"version\":1,\"method\":\"POST\",\"url\":\"https://example.com\"}", HttpInvokeJobParams.class);

		assertThat(actual.getMethod()).isEqualTo(HttpMethod.POST);
		assertThat(actual.getUrl()).isEqualTo("https://example.com");
	}

	@Test
	@DisplayName("requireRawJson + parseAndValidate：应从 JobDataMap 读取并解析 jobParams")
	void parseAndValidate_fromContext_readsRawJsonFromJobDataMap() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(SysJobQuartzDataKeys.JOB_PARAMS, "{\"version\":1,\"name\":\"from-map\"}");
		when(context.getMergedJobDataMap()).thenReturn(jobDataMap);

		SampleParams actual = parser.parseAndValidate(BuiltinJobParamsParser.requireRawJson(context),
				SampleParams.class);

		assertThat(actual.getName()).isEqualTo("from-map");
	}

	@Test
	@DisplayName("requireRawJson：job_params 为空时应抛出 SysJobException")
	void requireRawJson_whenMissing_throws() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());

		assertThatThrownBy(() -> BuiltinJobParamsParser.requireRawJson(context)).isInstanceOf(SysJobException.class);
	}

	@Getter
	@Setter
	private static class SampleParams implements VersionedJobParams {

		@NotNull
		private Integer version;

		@NotBlank
		private String name;

	}

}
