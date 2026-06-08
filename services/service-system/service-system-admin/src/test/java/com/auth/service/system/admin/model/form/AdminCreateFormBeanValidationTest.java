package com.auth.service.system.admin.model.form;

import com.auth.common.data.model.enums.GenderStatus;
import com.auth.common.data.model.enums.UserStatus;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
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

	private static SysPermissionCreateForm validPermissionForm() {
		SysPermissionCreateForm form = new SysPermissionCreateForm();
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
		form.setEmail("new@example.com");
		form.setEmployeeNo("E100");
		form.setStatus(UserStatus.NORMAL.getCode());
		form.setGender(GenderStatus.MALE.getCode());
		return form;
	}

	@Test
	@DisplayName("SysPermissionCreateForm：权限码不符合格式时存在 Pattern 约束违反")
	void permissionCreateFormRejectsMalformedCode() {
		SysPermissionCreateForm form = validPermissionForm();
		form.setPermissionCode("Sys:user:add");

		Set<ConstraintViolation<SysPermissionCreateForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "permissionCode".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysPermissionCreateForm：合法权限码通过校验")
	void permissionCreateFormAcceptsWellFormedCode() {
		SysPermissionCreateForm form = validPermissionForm();
		form.setPermissionCode("sys:user:add");

		assertThat(validator.validate(form)).isEmpty();
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
	@DisplayName("SysUserForm：邮箱为空时存在 NotBlank 约束违反")
	void userCreateFormRejectsBlankEmail() {
		SysUserForm form = validUserForm();
		form.setEmail("  ");

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "email".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysUserForm：工号为空时存在 NotBlank 约束违反")
	void userCreateFormRejectsBlankEmployeeNo() {
		SysUserForm form = validUserForm();
		form.setEmployeeNo("");

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "employeeNo".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysUserForm：性别非法时存在 Max 约束违反")
	void userCreateFormRejectsInvalidGender() {
		SysUserForm form = validUserForm();
		form.setGender(9);

		Set<ConstraintViolation<SysUserForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "gender".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysUserForm：合法表单通过校验")
	void userCreateFormAcceptsWellFormedPayload() {
		assertThat(validator.validate(validUserForm())).isEmpty();
	}

}
