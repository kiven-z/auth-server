package com.auth.service.system.admin.model.form.user;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysUserAvatarUpdateForm} 校验单元测试
 */
@DisplayName("SysUserAvatarUpdateForm 校验")
class SysUserAvatarUpdateFormValidationTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	@DisplayName("合法表单通过校验")
	void validForm_passesValidation() {
		SysUserAvatarUpdateForm form = new SysUserAvatarUpdateForm();
		form.setUserId(7L);
		form.setAvatar("https://cdn.example.com/public/avatar/a.png");

		Set<ConstraintViolation<SysUserAvatarUpdateForm>> violations = validator.validate(form);

		assertThat(violations).isEmpty();
	}

	@Test
	@DisplayName("用户 ID 为空时校验失败")
	void blankUserId_failsValidation() {
		SysUserAvatarUpdateForm form = new SysUserAvatarUpdateForm();
		form.setAvatar("https://cdn.example.com/public/avatar/a.png");

		Set<ConstraintViolation<SysUserAvatarUpdateForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "userId".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("头像为空时校验失败")
	void blankAvatar_failsValidation() {
		SysUserAvatarUpdateForm form = new SysUserAvatarUpdateForm();
		form.setAvatar("  ");

		Set<ConstraintViolation<SysUserAvatarUpdateForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "avatar".equals(v.getPropertyPath().toString()));
	}

}
