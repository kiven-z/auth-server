package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.mapper.SysMenuRoleMapper;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link SysMenuManageServiceImpl} 单元测试。
 */
@DisplayName("SysMenuManageServiceImpl 菜单角色分配")
@ExtendWith(MockitoExtension.class)
class SysMenuManageServiceImplTest {

	@Mock
	private SysMenuRoleMapper sysMenuRoleMapper;

	@Mock
	private RoleGrantReferenceChecker roleGrantReferenceChecker;

	@InjectMocks
	private SysMenuManageServiceImpl sysMenuManageService;

	@Test
	@DisplayName("覆盖分配：校验通过后删除旧关联并批量插入")
	void replaceMenuRolesReplacesAssociationsWhenEligible() {
		Long menuId = 5L;
		SysMenuAssignRoleForm form = new SysMenuAssignRoleForm();
		form.setRoleCodes(List.of("MENU_ADMIN", "MENU_GUEST"));

		sysMenuManageService.replaceMenuRoles(menuId, form);

		verify(roleGrantReferenceChecker).requireGrantEligibleRoles(List.of("MENU_ADMIN", "MENU_GUEST"),
				RoleTypeEnum.MENU_ASSIGN_TYPES, SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(sysMenuRoleMapper).deleteByMenuId(menuId);
		verify(sysMenuRoleMapper).batchInsertMenuRoles(menuId, List.of("MENU_ADMIN", "MENU_GUEST"));
	}

	@Test
	@DisplayName("覆盖分配：空列表仅删除关联不插入")
	void replaceMenuRolesWithEmptyListDeletesOnly() {
		Long menuId = 5L;
		SysMenuAssignRoleForm form = new SysMenuAssignRoleForm();
		form.setRoleCodes(List.of());

		sysMenuManageService.replaceMenuRoles(menuId, form);

		verify(roleGrantReferenceChecker).requireGrantEligibleRoles(List.of(), RoleTypeEnum.MENU_ASSIGN_TYPES,
				SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(sysMenuRoleMapper).deleteByMenuId(menuId);
		verify(sysMenuRoleMapper, never()).batchInsertMenuRoles(any(), any());
	}

	@Test
	@DisplayName("覆盖分配：角色校验失败时不写 sys_menu_role")
	void replaceMenuRolesThrowsWhenRoleInvalid() {
		Long menuId = 5L;
		SysMenuAssignRoleForm form = new SysMenuAssignRoleForm();
		form.setRoleCodes(List.of("BIZ_ADMIN"));

		doThrow(new SystemBusinessException(SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.when(roleGrantReferenceChecker)
			.requireGrantEligibleRoles(eq(List.of("BIZ_ADMIN")), eq(RoleTypeEnum.MENU_ASSIGN_TYPES),
					eq(SystemCommonResultCode.GRANT_REFERENCE_INVALID));

		assertThatThrownBy(() -> sysMenuManageService.replaceMenuRoles(menuId, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);

		verify(sysMenuRoleMapper, never()).deleteByMenuId(any());
		verify(sysMenuRoleMapper, never()).batchInsertMenuRoles(any(), any());
	}

}
