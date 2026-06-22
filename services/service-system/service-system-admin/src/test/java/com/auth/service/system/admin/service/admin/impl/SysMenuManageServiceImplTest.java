package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.mapper.menu.SysMenuPermissionMapper;
import com.auth.service.system.admin.mapper.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignPermissionForm;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.support.permission.PermissionReferenceChecker;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link SysMenuManageServiceImpl} 单元测试。
 */
@DisplayName("SysMenuManageServiceImpl 菜单分配")
@ExtendWith(MockitoExtension.class)
class SysMenuManageServiceImplTest {

	@Mock
	private SysMenuRoleMapper sysMenuRoleMapper;

	@Mock
	private SysMenuPermissionMapper sysMenuPermissionMapper;

	@Mock
	private RoleGrantReferenceChecker roleGrantReferenceChecker;

	@Mock
	private PermissionReferenceChecker permissionReferenceChecker;

	@InjectMocks
	private SysMenuManageServiceImpl sysMenuManageService;

	@Test
	@DisplayName("覆盖分配：校验通过后删除旧关联并批量插入")
	void replaceMenuRolesReplacesAssociationsWhenEligible() {
		Long menuId = 5L;
		SysMenuAssignRoleForm form = new SysMenuAssignRoleForm();
		form.setRoleIds(List.of(1L, 2L));

		sysMenuManageService.replaceMenuRoles(menuId, form);

		verify(roleGrantReferenceChecker).requireExistingEnabledRoleIds(List.of(1L, 2L),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(sysMenuRoleMapper).deleteByMenuId(menuId);
		verify(sysMenuRoleMapper).batchInsertMenuRoles(menuId, List.of(1L, 2L));
	}

	@Test
	@DisplayName("覆盖分配：空列表仅删除关联不插入")
	void replaceMenuRolesWithEmptyListDeletesOnly() {
		Long menuId = 5L;
		SysMenuAssignRoleForm form = new SysMenuAssignRoleForm();
		form.setRoleIds(List.of());

		sysMenuManageService.replaceMenuRoles(menuId, form);

		verify(roleGrantReferenceChecker).requireExistingEnabledRoleIds(List.of(),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(sysMenuRoleMapper).deleteByMenuId(menuId);
		verify(sysMenuRoleMapper, never()).batchInsertMenuRoles(any(), any());
	}

	@Test
	@DisplayName("覆盖分配：角色校验失败时不写 sys_menu_role")
	void replaceMenuRolesThrowsWhenRoleInvalid() {
		Long menuId = 5L;
		SysMenuAssignRoleForm form = new SysMenuAssignRoleForm();
		form.setRoleIds(List.of(99L));

		doThrow(new SystemBusinessException(SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.when(roleGrantReferenceChecker)
			.requireExistingEnabledRoleIds(List.of(99L), SystemCommonResultCode.GRANT_REFERENCE_INVALID);

		assertThatThrownBy(() -> sysMenuManageService.replaceMenuRoles(menuId, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);

		verify(sysMenuRoleMapper, never()).deleteByMenuId(any());
		verify(sysMenuRoleMapper, never()).batchInsertMenuRoles(any(), any());
	}

	@Test
	@DisplayName("覆盖分配权限：校验通过后删除旧关联并批量插入 permission_id")
	void replaceMenuPermissionsReplacesAssociationsWhenEligible() {
		Long menuId = 5L;
		SysMenuAssignPermissionForm form = new SysMenuAssignPermissionForm();
		form.setPermissionCodes(List.of("sys:user:create", "sys:user:query"));
		when(permissionReferenceChecker.resolveActivePermissionIds(List.of("sys:user:create", "sys:user:query")))
			.thenReturn(List.of(10L, 20L));

		sysMenuManageService.replaceMenuPermissions(menuId, form);

		verify(permissionReferenceChecker).resolveActivePermissionIds(List.of("sys:user:create", "sys:user:query"));
		verify(sysMenuPermissionMapper).deleteByMenuId(menuId);
		verify(sysMenuPermissionMapper).batchInsertMenuPermissions(menuId, List.of(10L, 20L));
	}

	@Test
	@DisplayName("覆盖分配权限：空列表仅删除关联不插入")
	void replaceMenuPermissionsWithEmptyListDeletesOnly() {
		Long menuId = 5L;
		SysMenuAssignPermissionForm form = new SysMenuAssignPermissionForm();
		form.setPermissionCodes(List.of());
		when(permissionReferenceChecker.resolveActivePermissionIds(List.of())).thenReturn(List.of());

		sysMenuManageService.replaceMenuPermissions(menuId, form);

		verify(permissionReferenceChecker).resolveActivePermissionIds(List.of());
		verify(sysMenuPermissionMapper).deleteByMenuId(menuId);
		verify(sysMenuPermissionMapper, never()).batchInsertMenuPermissions(any(), any());
	}

	@Test
	@DisplayName("覆盖分配权限：权限校验失败时不写 sys_menu_permission")
	void replaceMenuPermissionsThrowsWhenPermissionInvalid() {
		Long menuId = 5L;
		SysMenuAssignPermissionForm form = new SysMenuAssignPermissionForm();
		form.setPermissionCodes(List.of("missing:perm"));
		when(permissionReferenceChecker.resolveActivePermissionIds(List.of("missing:perm")))
			.thenThrow(new SystemBusinessException(SystemCommonResultCode.GRANT_REFERENCE_INVALID));

		assertThatThrownBy(() -> sysMenuManageService.replaceMenuPermissions(menuId, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);

		verify(sysMenuPermissionMapper, never()).deleteByMenuId(any());
		verify(sysMenuPermissionMapper, never()).batchInsertMenuPermissions(any(), any());
	}

}
