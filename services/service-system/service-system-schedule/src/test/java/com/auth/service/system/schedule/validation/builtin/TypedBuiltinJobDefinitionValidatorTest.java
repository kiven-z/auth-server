package com.auth.service.system.schedule.validation.builtin;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.support.builtin.BuiltinJobParamsParser;
import com.auth.service.system.schedule.task.builtin.HttpInvokeJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link TypedBuiltinJobDefinitionValidator} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("TypedBuiltinJobDefinitionValidator 内置 job_params 校验")
class TypedBuiltinJobDefinitionValidatorTest {

	private TypedBuiltinJobDefinitionValidator validator;

	@BeforeEach
	void setUp() {
		Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
		BuiltinJobParamsParser parser = new BuiltinJobParamsParser(new ObjectMapper(), beanValidator);
		validator = new TypedBuiltinJobDefinitionValidator(HttpInvokeJob.class, HttpInvokeJobParams.class, parser);
	}

	@Test
	@DisplayName("jobClass：应返回内置 Job 全限定名")
	void jobClass_returnsBuiltinJobClassName() {
		assertThat(validator.jobClass()).isEqualTo(HttpInvokeJob.class.getName());
	}

	@Test
	@DisplayName("validateJobParams：合法 JSON 应通过")
	void validateJobParams_whenValid_passes() {
		String json = "{\"version\":1,\"method\":\"GET\",\"url\":\"https://example.com\"}";

		assertThatCode(() -> validator.validateJobParams(json)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("validateJobParams：缺少 url 应抛出 SysJobException")
	void validateJobParams_whenUrlMissing_throws() {
		String json = "{\"version\":1,\"method\":\"GET\"}";

		assertThatThrownBy(() -> validator.validateJobParams(json)).isInstanceOf(SysJobException.class);
	}

}
