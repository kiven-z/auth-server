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
 * {@link SmsProperties} Bean Validation 单元测试
 */
@DisplayName("SmsProperties 配置校验")
class SmsPropertiesValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	@DisplayName("必填项齐全时校验通过")
	void validate_shouldPassWhenAllRequiredFieldsPresent() {
		SmsProperties properties = validProperties();

		Set<ConstraintViolation<SmsProperties>> violations = validator.validate(properties);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("signName 为空时校验失败")
	void validate_shouldFailWhenSignNameBlank() {
		SmsProperties properties = validProperties();
		properties.setSignName("  ");

		Set<ConstraintViolation<SmsProperties>> violations = validator.validate(properties);

		assertThat(violations).anyMatch(v -> "signName".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("accessKeyId 缺失时校验失败")
	void validate_shouldFailWhenAccessKeyIdMissing() {
		SmsProperties properties = validProperties();
		properties.setAccessKeyId(null);

		Set<ConstraintViolation<SmsProperties>> violations = validator.validate(properties);

		assertThat(violations).anyMatch(v -> "accessKeyId".equals(v.getPropertyPath().toString()));
	}

	private static SmsProperties validProperties() {
		SmsProperties properties = new SmsProperties();
		properties.setRegion("cn-hangzhou");
		properties.setEndpoint("dypnsapi.aliyuncs.com");
		properties.setAccessKeyId("test-ak");
		properties.setAccessKeySecret("test-sk");
		properties.setSignName("TestSign");
		return properties;
	}

}
