package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.user.SysUserMapper;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.admin.GrantTableService;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SysUserRoleServiceImpl} 单元测试
 */
@DisplayName("SysUserRoleServiceImpl 用户直连角色授权")
@ExtendWith(MockitoExtension.class)
class SysUserRoleServiceImplTest {

	private static final Long USER_ID = 10L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private UserOperationChecker userOperationChecker;

	@Mock
	private RoleGrantReferenceChecker roleGrantReferenceChecker;

	@Mock
	private GrantTableService grantTableService;

	@Mock
	private UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	private SysUserRoleServiceImpl sysUserRoleService;

	private static UserEntity activeUser() {
		UserEntity entity = new UserEntity();
		entity.setId(SysUserRoleServiceImplTest.USER_ID);
		entity.setUsername("tester");
		return entity;
	}

	@BeforeEach
	void setUp() {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		sysUserRoleService = new SysUserRoleServiceImpl(userReferenceChecker, userOperationChecker,
				roleGrantReferenceChecker, grantTableService, userAuthorizationInvalidationTrigger);
	}

	@Test
	@DisplayName("查询已分配角色：用户存在时返回转换结果")
	void listAssignedRolesReturnsRowsWhenUserExists() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		GrantTableAssignedRoleRow row = new GrantTableAssignedRoleRow();
		row.setRoleCode("CUSTOM_A");
		row.setRoleName("业务角色A");
		when(grantTableService.listBoundRoles(GrantTableSubjectType.USER, USER_ID)).thenReturn(List.of(row));

		List<GrantTableAssignedRoleRow> rows = sysUserRoleService.listAssignedRoles(USER_ID);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("CUSTOM_A");
		assertThat(rows.get(0).getRoleName()).isEqualTo("业务角色A");
	}

	@Test
	@DisplayName("查询已分配角色：用户不存在时抛出 USER_NOT_FOUND")
	void listAssignedRolesThrowsWhenUserMissing() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(null);

		assertThatThrownBy(() -> sysUserRoleService.listAssignedRoles(USER_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("全量覆盖：写入 grant_table 并提交用户授权失效")
	void replaceUserRolesWritesGrantAndSubmitsInvalidation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of("BIZ_ADMIN", "CUSTOM_A"));

		sysUserRoleService.replaceUserRoles(USER_ID, form);

		verify(userOperationChecker).requireOperable(List.of(USER_ID));
		verify(roleGrantReferenceChecker).requireExistingEnabledRoles(List.of("BIZ_ADMIN", "CUSTOM_A"),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(grantTableService).replaceSubjectRoleGrants(GrantTableSubjectType.USER.name(), USER_ID,
				List.of("BIZ_ADMIN", "CUSTOM_A"));
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "replace-roles");
	}

	@Test
	@DisplayName("全量覆盖：空角色列表仅删除 grant 行仍触发用户失效")
	void replaceUserRolesClearsGrantsWhenRoleCodesEmpty() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of());

		sysUserRoleService.replaceUserRoles(USER_ID, form);

		verify(grantTableService).replaceSubjectRoleGrants(GrantTableSubjectType.USER.name(), USER_ID, List.of());
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "replace-roles");
	}

	@Test
	@DisplayName("全量覆盖：受保护用户不允许分配角色")
	void replaceUserRolesThrowsWhenUserNotOperable() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser());
		doThrow(new SystemBusinessException(SystemAdminResultCode.USER_OPERATION_FORBIDDEN, USER_ID))
			.when(userOperationChecker)
			.requireOperable(List.of(USER_ID));
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of("CUSTOM_A"));

		assertThatThrownBy(() -> sysUserRoleService.replaceUserRoles(USER_ID, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);

		verify(grantTableService, never()).replaceSubjectRoleGrants(any(), any(), any());
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

}
