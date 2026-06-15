package com.auth.service.system.schedule.task.builtin.http.validation;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.task.builtin.support.BuiltinJobParamsParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link HttpPostBuiltinJobDefinitionValidator} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("HttpPostBuiltinJobDefinitionValidator 保存时 job_params 校验")
class HttpPostBuiltinJobDefinitionValidatorTest {

	private HttpPostBuiltinJobDefinitionValidator validator;

	@BeforeEach
	void setUp() {
		Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
		BuiltinJobParamsParser parser = new BuiltinJobParamsParser(new ObjectMapper(), beanValidator);
		validator = new HttpPostBuiltinJobDefinitionValidator(parser);
	}

	@Test
	@DisplayName("validateJobParams：合法 JSON 应通过")
	void validateJobParams_whenValid_passes() {
		String json = "{\"version\":1,\"url\":\"https://example.com\",\"body\":\"{}\"}";

		assertThatCode(() -> validator.validateJobParams(json)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("validateJobParams：缺少 url 应抛出 SysJobException")
	void validateJobParams_whenUrlMissing_throws() {
		String json = "{\"version\":1,\"body\":\"{}\"}";

		assertThatThrownBy(() -> validator.validateJobParams(json)).isInstanceOf(SysJobException.class);
	}

}
