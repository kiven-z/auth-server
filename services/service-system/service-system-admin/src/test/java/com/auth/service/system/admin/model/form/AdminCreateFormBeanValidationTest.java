package com.auth.service.system.admin.model.form;

import com.auth.common.data.model.enums.GenderStatus;
import com.auth.common.data.model.enums.UserStatus;
import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 新增类表单 Bean Validation（与全局 JSON strip 配合：{@code @Pattern} 在 {@code @Valid} 入口生效）。
 *
 * @author Bunny
 */
@DisplayName("管理端新增表单 Bean 校验")
class AdminCreateFormBeanValidationTest {

	private static Validator validator;

	@BeforeAll
	static void initValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	private static SysPermissionForm validPermissionForm() {
		SysPermissionForm form = new SysPermissionForm();
		form.setPermissionCode("sys:user:add");
		form.setPermissionName("新增用户");
		form.setPermissionType(1);
		form.setRemark("r");
		return form;
	}

	private static SysRoleForm validRoleForm() {
		SysRoleForm form = new SysRoleForm();
		form.setRoleCode("CUSTOM_R_ONE");
		form.setRoleName("自定义");
		form.setStatus(true);
		form.setRoleType(4);
		form.setOrderNum(0);
		return form;
	}

	private static SysUserForm validUserForm() {
		SysUserForm form = new SysUserForm();
		form.setUsername("new_user");
		form.setNickname("New User");
		form.setEmail("new@example.com");
		form.setPhone("13800000000");
		form.setEmployeeNo("E100");
		form.setStatus(UserStatus.NORMAL.getCode());
		form.setGender(GenderStatus.MALE.getCode());
		return form;
	}

	@Test
	@DisplayName("SysPermissionForm：创建时权限码不符合格式时存在 Pattern 约束违反")
	void permissionCreateFormRejectsMalformedCode() {
		SysPermissionForm form = validPermissionForm();
		form.setPermissionCode("Sys:user:add");

		Set<ConstraintViolation<SysPermissionForm>> violations = validator.validate(form, CreateGroup.class);

		assertThat(violations).anyMatch(v -> "permissionCode".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysPermissionForm：创建时合法权限码通过校验")
	void permissionCreateFormAcceptsWellFormedCode() {
		SysPermissionForm form = validPermissionForm();
		form.setPermissionCode("sys:user:add");

		assertThat(validator.validate(form, CreateGroup.class)).isEmpty();
	}

	@Test
	@DisplayName("SysPermissionForm：更新时状态为空时存在 NotNull 约束违反")
	void permissionUpdateFormRejectsNullStatus() {
		SysPermissionForm form = validPermissionForm();
		form.setId(1L);
		form.setStatus(null);

		Set<ConstraintViolation<SysPermissionForm>> violations = validator.validate(form, UpdateGroup.class);

		assertThat(violations).anyMatch(v -> "status".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysPermissionForm：更新时 ID 为空时存在 NotNull 约束违反")
	void permissionUpdateFormRejectsNullId() {
		SysPermissionForm form = validPermissionForm();
		form.setStatus(true);

		Set<ConstraintViolation<SysPermissionForm>> violations = validator.validate(form, UpdateGroup.class);

		assertThat(violations).anyMatch(v -> "id".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysRoleSaveForm：角色码不符合格式时存在 Pattern 约束违反")
	void roleCreateFormRejectsMalformedCode() {
		SysRoleForm form = validRoleForm();
		form.setRoleCode("admin");

		Set<ConstraintViolation<SysRoleForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "roleCode".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysRoleSaveForm：合法角色码通过校验")
	void roleCreateFormAcceptsWellFormedCode() {
		SysRoleForm form = validRoleForm();
		form.setRoleCode("CUSTOM_R_ONE");

		assertThat(validator.validate(form)).isEmpty();
	}

	@Test
	@DisplayName("SysUserForm：新增时用户名为空时存在 NotBlank 约束违反")
	void userCreateFormRejectsBlankUsername() {
		SysUserForm form = validUserForm();
		form.setUsername("  ");

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form, CreateGroup.class);

		assertThat(violations).anyMatch(v -> "username".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysUserForm：新增时账号状态为空时存在 NotNull 约束违反")
	void userCreateFormRejectsNullStatus() {
		SysUserForm form = validUserForm();
		form.setStatus(null);

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form, CreateGroup.class);

		assertThat(violations).anyMatch(v -> "status".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysUserForm：新增时邮箱为空时存在 NotBlank 约束违反")
	void userCreateFormRejectsBlankEmail() {
		SysUserForm form = validUserForm();
		form.setEmail("  ");

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form, CreateGroup.class);

		assertThat(violations).anyMatch(v -> "email".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysUserForm：新增时昵称为空时存在 NotBlank 约束违反")
	void userCreateFormRejectsBlankNickname() {
		SysUserForm form = validUserForm();
		form.setNickname("  ");

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form, CreateGroup.class);

		assertThat(violations).anyMatch(v -> "nickname".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysUserForm：新增时手机号为空时存在 NotBlank 约束违反")
	void userCreateFormRejectsBlankPhone() {
		SysUserForm form = validUserForm();
		form.setPhone("");

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form, CreateGroup.class);

		assertThat(violations).anyMatch(v -> "phone".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysUserForm：新增时工号为空时通过校验")
	void userCreateFormAcceptsBlankEmployeeNo() {
		SysUserForm form = validUserForm();
		form.setEmployeeNo(null);

		assertThat(validator.validate(form, CreateGroup.class)).isEmpty();
	}

	@Test
	@DisplayName("SysUserForm：新增时性别非法时存在 Max 约束违反")
	void userCreateFormRejectsInvalidGender() {
		SysUserForm form = validUserForm();
		form.setGender(9);

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form, CreateGroup.class);

		assertThat(violations).anyMatch(v -> "gender".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysUserForm：新增时合法表单通过校验")
	void userCreateFormAcceptsWellFormedPayload() {
		assertThat(validator.validate(validUserForm(), CreateGroup.class)).isEmpty();
	}

	@Test
	@DisplayName("SysUserForm：更新时缺少用户名与状态仍可通过校验")
	void userUpdateFormAcceptsPayloadWithoutUsernameOrStatus() {
		SysUserForm form = validUserForm();
		form.setUsername(null);
		form.setStatus(null);

		assertThat(validator.validate(form, UpdateGroup.class)).isEmpty();
	}

	@Test
	@DisplayName("SysUserForm：更新时昵称为空时存在 NotBlank 约束违反")
	void userUpdateFormRejectsBlankNickname() {
		SysUserForm form = validUserForm();
		form.setUsername(null);
		form.setStatus(null);
		form.setNickname("");

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form, UpdateGroup.class);

		assertThat(violations).anyMatch(v -> "nickname".equals(v.getPropertyPath().toString()));
	}

}
