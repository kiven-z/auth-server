package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.mapper.permission.SysPermissionMapper;
import com.auth.service.system.admin.mapper.role.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.vo.reference.PermissionReferenceVO;
import com.auth.service.system.admin.support.permission.PermissionReferenceChecker;
import com.auth.service.system.admin.support.role.RoleReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * {@link SysRolePermissionServiceImpl} 单元测试。
 */
@DisplayName("SysRolePermissionServiceImpl 角色权限分配")
@ExtendWith(MockitoExtension.class)
class SysRolePermissionServiceImplTest {

	@Mock
	private RoleReferenceChecker roleReferenceChecker;

	@Mock
	private SysRolePermissionMapper sysRolePermissionMapper;

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	private SysRolePermissionServiceImpl sysRolePermissionService;

	@BeforeEach
	void setUp() {
		PermissionReferenceChecker permissionReferenceChecker = new PermissionReferenceChecker(sysPermissionMapper);
		sysRolePermissionService = new SysRolePermissionServiceImpl(roleReferenceChecker, sysRolePermissionMapper,
				permissionReferenceChecker, roleInvalidationTrigger);
	}

	@Test
	@DisplayName("查询已分配权限：返回权限码与名称")
	void listAssignedPermissionsShouldReturnCodeAndName() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(1L);
		role.setRoleCode("ADMIN");
		when(roleReferenceChecker.getExistingActive(1L)).thenReturn(role);

		PermissionReferencePO po = new PermissionReferencePO();
		po.setId(10L);
		po.setPermissionCode("sys:user:list");
		po.setPermissionName("用户查询");
		when(sysRolePermissionMapper.selectAssignedPermissionsByRoleId(1L)).thenReturn(List.of(po));

		List<PermissionReferenceVO> rows = sysRolePermissionService.listAssignedPermissions(1L);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getId()).isEqualTo(10L);
		assertThat(rows.get(0).getPermissionCode()).isEqualTo("sys:user:list");
		assertThat(rows.get(0).getPermissionName()).isEqualTo("用户查询");
	}

	@Test
	@DisplayName("分配权限：角色不存在时抛出 DATA_NOT_EXIST")
	void assignWhenRoleMissingShouldThrow() {
		when(roleReferenceChecker.getExistingActive(99L))
			.thenThrow(new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, 99L));

		List<Long> permissionIds = List.of(10L);
		assertThatThrownBy(() -> sysRolePermissionService.assignPermissions(99L, permissionIds))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("分配权限：全量替换后触发 ROLE 失效")
	void assignShouldReplaceAndInvalidate() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(1L);
		role.setRoleCode("ADMIN");
		when(roleReferenceChecker.getExistingActive(1L)).thenReturn(role);
		when(sysPermissionMapper.selectCount(any())).thenReturn(1L);

		sysRolePermissionService.assignPermissions(1L, List.of(10L));

		verify(sysRolePermissionMapper).deleteByRoleId(1L);
		verify(sysRolePermissionMapper).batchInsertRolePermissions(eq(1L), eq(List.of(10L)), isNull(), isNull());
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("ADMIN"), "assign-permissions");
	}

	@Test
	@DisplayName("分配权限：空列表仅清空关联且不校验权限表")
	void assignEmptyListShouldClearOnly() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(1L);
		role.setRoleCode("ADMIN");
		when(roleReferenceChecker.getExistingActive(1L)).thenReturn(role);

		sysRolePermissionService.assignPermissions(1L, List.of());

		verify(sysRolePermissionMapper).deleteByRoleId(1L);
		verify(sysPermissionMapper, never()).selectCount(any());
		verify(sysRolePermissionMapper, never()).batchInsertRolePermissions(any(), any(), any(), any());
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("ADMIN"), "assign-permissions");
	}

	@Test
	@DisplayName("分配权限：权限 ID 无效时抛出 GRANT_REFERENCE_INVALID")
	void assignWhenPermissionInvalidShouldThrow() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(1L);
		role.setRoleCode("ADMIN");
		when(roleReferenceChecker.getExistingActive(1L)).thenReturn(role);
		when(sysPermissionMapper.selectCount(any())).thenReturn(0L);

		List<Long> permissionIds = List.of(99L);
		assertThatThrownBy(() -> sysRolePermissionService.assignPermissions(1L, permissionIds))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);

		verify(sysRolePermissionMapper).deleteByRoleId(1L);
		verify(sysRolePermissionMapper, never()).batchInsertRolePermissions(any(), any(), any(), any());
	}

}
