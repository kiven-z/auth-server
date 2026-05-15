package com.auth.service.system.admin.model.form;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.form.role.SysRoleCreateForm;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

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
	@DisplayName("SysRoleCreateForm：角色码不符合格式时存在 Pattern 约束违反")
	void roleCreateFormRejectsMalformedCode() {
		SysRoleCreateForm form = validRoleForm();
		form.setRoleCode("admin");

		Set<ConstraintViolation<SysRoleCreateForm>> violations = validator.validate(form);

		assertThat(violations).anyMatch(v -> "roleCode".equals(v.getPropertyPath().toString()));
	}

	@Test
	@DisplayName("SysRoleCreateForm：合法角色码通过校验")
	void roleCreateFormAcceptsWellFormedCode() {
		SysRoleCreateForm form = validRoleForm();
		form.setRoleCode("CUSTOM_R_ONE");

		assertThat(validator.validate(form)).isEmpty();
	}

	private static SysPermissionCreateForm validPermissionForm() {
		SysPermissionCreateForm form = new SysPermissionCreateForm();
		form.setPermissionCode("sys:user:add");
		form.setPermissionName("新增用户");
		form.setPermissionType(1);
		form.setRemark("r");
		return form;
	}

	private static SysRoleCreateForm validRoleForm() {
		SysRoleCreateForm form = new SysRoleCreateForm();
		form.setRoleCode("CUSTOM_R_ONE");
		form.setRoleName("自定义");
		form.setStatus(true);
		form.setRoleType(4);
		form.setOrderNum(0);
		return form;
	}

}
