package com.auth.service.system.admin.model.form.me;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MeProfileUpdateForm} 校验单元测试
 */
@DisplayName("MeProfileUpdateForm 校验")
class MeProfileUpdateFormValidationTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private static MeProfileUpdateForm validForm() {
		MeProfileUpdateForm form = new MeProfileUpdateForm();
		form.setNickname("Alice");
		form.setEmail("alice@example.com");
		form.setPhone("13800000000");
		form.setGender(1);
		form.setBirthday(LocalDate.of(1990, 1, 1));
		form.setIntroduction("Hello");
		return form;
	}

	@Test
	@DisplayName("合法表单通过校验")
	void validForm_passesValidation() {
		MeProfileUpdateForm form = validForm();

		Set<ConstraintViolation<MeProfileUpdateForm>> violations = validator.validate(form);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("昵称为空时校验失败")
	void blankNickname_failsValidation() {
		MeProfileUpdateForm form = validForm();
		form.setNickname("  ");

		Set<ConstraintViolation<MeProfileUpdateForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "nickname".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("邮箱格式不正确时校验失败")
	void invalidEmail_failsValidation() {
		MeProfileUpdateForm form = validForm();
		form.setEmail("not-an-email");

		Set<ConstraintViolation<MeProfileUpdateForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "email".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("出生日期晚于今天时校验失败")
	void futureBirthday_failsValidation() {
		MeProfileUpdateForm form = validForm();
		form.setBirthday(LocalDate.now().plusDays(1));

		Set<ConstraintViolation<MeProfileUpdateForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "birthday".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("性别超出范围时校验失败")
	void invalidGender_failsValidation() {
		MeProfileUpdateForm form = validForm();
		form.setGender(3);

		Set<ConstraintViolation<MeProfileUpdateForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "gender".equals(v.getPropertyPath().toString()));
	}

}
