package com.auth.service.system.admin.model.form.me;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MeAvatarUpdateForm} 校验单元测试
 */
@DisplayName("MeAvatarUpdateForm 校验")
class MeAvatarUpdateFormValidationTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	@DisplayName("合法表单通过校验")
	void validForm_passesValidation() {
		MeAvatarUpdateForm form = new MeAvatarUpdateForm();
		form.setAvatar("https://cdn.example.com/public/avatar/a.png");

		Set<ConstraintViolation<MeAvatarUpdateForm>> violations = validator.validate(form);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("头像为空时校验失败")
	void blankAvatar_failsValidation() {
		MeAvatarUpdateForm form = new MeAvatarUpdateForm();
		form.setAvatar("  ");

		Set<ConstraintViolation<MeAvatarUpdateForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "avatar".equals(v.getPropertyPath().toString()));
	}

}
