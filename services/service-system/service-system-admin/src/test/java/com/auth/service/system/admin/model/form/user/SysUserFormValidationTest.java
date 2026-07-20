package com.auth.service.system.admin.model.form.user;

import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysUserForm} 校验单元测试
 */
@DisplayName("SysUserForm 校验")
class SysUserFormValidationTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private static SysUserForm validCreateForm() {
		SysUserForm form = new SysUserForm();
		form.setUsername("alice");
		form.setNickname("Alice");
		form.setEmail("alice@example.com");
		form.setPhone("13800000000");
		form.setStatus(1);
		form.setInitialPassword("SecurePwd1");
		return form;
	}

	@Test
	@DisplayName("新增时初始密码不能为空")
	void createGroup_requiresInitialPassword() {
		SysUserForm form = validCreateForm();
		form.setInitialPassword(null);

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form, CreateGroup.class);

		assertThat(violations).anyMatch(v -> "initialPassword".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("更新时不校验初始密码")
	void updateGroup_ignoresInitialPassword() {
		SysUserForm form = validCreateForm();
		form.setInitialPassword(null);

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form, UpdateGroup.class);

		assertThat(violations).noneMatch(v -> "initialPassword".equals(v.getPropertyPath().toString()));
	}

}
