package com.auth.service.system.schedule.task.builtin.feign.validation;

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
 * {@link FeignInvokeBuiltinJobDefinitionValidator} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FeignInvokeBuiltinJobDefinitionValidator 保存时 job_params 校验")
class FeignInvokeBuiltinJobDefinitionValidatorTest {

	private FeignInvokeBuiltinJobDefinitionValidator validator;

	@BeforeEach
	void setUp() {
		Validator beanValidator = Validation.buildDefaultValidatorFactory().getValidator();
		BuiltinJobParamsParser parser = new BuiltinJobParamsParser(new ObjectMapper(), beanValidator);
		validator = new FeignInvokeBuiltinJobDefinitionValidator(parser);
	}

	@Test
	@DisplayName("validateJobParams：合法 JSON 应通过")
	void validateJobParams_whenValid_passes() {
		String json = """
				{"version":1,"serviceName":"order-service","path":"/api/sync","method":"POST","body":"{}"}
				""";

		assertThatCode(() -> validator.validateJobParams(json)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("validateJobParams：缺少 serviceName 应抛出 SysJobException")
	void validateJobParams_whenServiceNameMissing_throws() {
		String json = "{\"version\":1,\"path\":\"/api/sync\",\"method\":\"GET\"}";

		assertThatThrownBy(() -> validator.validateJobParams(json)).isInstanceOf(SysJobException.class);
	}

	@Test
	@DisplayName("validateJobParams：非法 method 应抛出 SysJobException")
	void validateJobParams_whenMethodInvalid_throws() {
		String json = "{\"version\":1,\"serviceName\":\"order-service\",\"path\":\"/api/sync\",\"method\":\"PATCH\"}";

		assertThatThrownBy(() -> validator.validateJobParams(json)).isInstanceOf(SysJobException.class);
	}

}
