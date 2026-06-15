package com.auth.service.system.schedule.task.builtin.support;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.task.builtin.model.VersionedJobParams;
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
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link BuiltinJobParamsParser} 与 {@link BuiltinJobParamsSupport} 单元测试。
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
	@DisplayName("parseFromContext：应从 JobDataMap 读取 jobParams 原文")
	void parseFromContext_readsRawJsonFromJobDataMap() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		JobDetail jobDetail = mock(JobDetail.class);
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(SysJobQuartzDataKeys.JOB_PARAMS, "{\"version\":1,\"name\":\"from-map\"}");
		when(context.getMergedJobDataMap()).thenReturn(jobDataMap);
		when(context.getJobDetail()).thenReturn(jobDetail);

		SampleParams actual = parser.parseFromContext(context, SampleParams.class);

		assertThat(actual.getName()).isEqualTo("from-map");
	}

	@Test
	@DisplayName("requireRawJson：job_params 为空时应抛出 SysJobException")
	void requireRawJson_whenMissing_throws() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());

		assertThatThrownBy(() -> BuiltinJobParamsSupport.requireRawJson(context)).isInstanceOf(SysJobException.class);
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
