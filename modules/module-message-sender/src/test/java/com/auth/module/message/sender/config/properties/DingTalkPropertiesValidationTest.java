package com.auth.module.message.sender.config.properties;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DingTalkProperties} Bean Validation 单元测试
 */
@DisplayName("DingTalkProperties 配置校验")
class DingTalkPropertiesValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	@DisplayName("必填项齐全时校验通过")
	void validate_shouldPassWhenAllRequiredFieldsPresent() {
		DingTalkProperties properties = validProperties();

		Set<ConstraintViolation<DingTalkProperties>> violations = validator.validate(properties);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("clientSecret 为空时校验失败")
	void validate_shouldFailWhenClientSecretBlank() {
		DingTalkProperties properties = validProperties();
		properties.setClientSecret("");

		Set<ConstraintViolation<DingTalkProperties>> violations = validator.validate(properties);

		assertThat(violations).anyMatch(v -> "clientSecret".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("agentId 缺失时校验失败")
	void validate_shouldFailWhenAgentIdMissing() {
		DingTalkProperties properties = validProperties();
		properties.setAgentId(null);

		Set<ConstraintViolation<DingTalkProperties>> violations = validator.validate(properties);

		assertThat(violations).anyMatch(v -> "agentId".equals(v.getPropertyPath().toString()));
	}

	private static DingTalkProperties validProperties() {
		DingTalkProperties properties = new DingTalkProperties();
		properties.setAgentId(123456789L);
		properties.setClientId("ding-client-id");
		properties.setClientSecret("ding-client-secret");
		return properties;
	}

}
