package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.mapper.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.po.role.SysRoleAssignedPermissionPO;
import com.auth.service.system.admin.model.vo.role.SysRoleAssignedPermissionRow;
import com.auth.service.system.admin.support.permission.PermissionReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysRolePermissionServiceImpl} 单元测试。
 */
@DisplayName("SysRolePermissionServiceImpl 角色权限分配")
@ExtendWith(MockitoExtension.class)
class SysRolePermissionServiceImplTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

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
		sysRolePermissionService = new SysRolePermissionServiceImpl(sysRoleMapper, sysRolePermissionMapper,
				permissionReferenceChecker, roleInvalidationTrigger);
	}

	@Test
	@DisplayName("查询已分配权限：返回权限码与名称")
	void listAssignedPermissionsShouldReturnCodeAndName() {
		SysRoleEntity role = new SysRoleEntity();
		role.setRoleCode("ADMIN");
		when(sysRoleMapper.selectOne(any())).thenReturn(role);

		SysRoleAssignedPermissionPO po = new SysRoleAssignedPermissionPO();
		po.setPermissionCode("sys:user:list");
		po.setPermissionName("用户查询");
		when(sysRolePermissionMapper.selectAssignedPermissionsByRoleCode("ADMIN")).thenReturn(List.of(po));

		List<SysRoleAssignedPermissionRow> rows = sysRolePermissionService.listAssignedPermissions("ADMIN");

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getPermissionCode()).isEqualTo("sys:user:list");
		assertThat(rows.get(0).getPermissionName()).isEqualTo("用户查询");
	}

	@Test
	@DisplayName("分配权限：角色不存在时抛出 ROLE_NOT_FOUND")
	void assignWhenRoleMissingShouldThrow() {
		when(sysRoleMapper.selectOne(any())).thenReturn(null);

		List<String> permissionCodes = List.of("sys:user:list");
		assertThatThrownBy(() -> sysRolePermissionService.assignPermissions("MISSING", permissionCodes))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.ROLE_NOT_FOUND);
	}

	@Test
	@DisplayName("分配权限：全量替换后触发 ROLE 失效")
	void assignShouldReplaceAndInvalidate() {
		SysRoleEntity role = new SysRoleEntity();
		role.setRoleCode("ADMIN");
		when(sysRoleMapper.selectOne(any())).thenReturn(role);
		when(sysPermissionMapper.selectCount(any())).thenReturn(1L);

		sysRolePermissionService.assignPermissions("ADMIN", List.of("sys:user:list"));

		verify(sysRolePermissionMapper).deleteByRoleCode("ADMIN");
		verify(sysRolePermissionMapper).batchInsertRolePermissions(eq("ADMIN"), eq(List.of("sys:user:list")), isNull(),
				isNull());
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("ADMIN"), "assign-permissions:ADMIN");
	}

	@Test
	@DisplayName("分配权限：空列表仅清空关联且不校验权限表")
	void assignEmptyListShouldClearOnly() {
		SysRoleEntity role = new SysRoleEntity();
		role.setRoleCode("ADMIN");
		when(sysRoleMapper.selectOne(any())).thenReturn(role);

		sysRolePermissionService.assignPermissions("ADMIN", List.of());

		verify(sysRolePermissionMapper).deleteByRoleCode("ADMIN");
		verify(sysPermissionMapper, never()).selectCount(any());
		verify(sysRolePermissionMapper, never()).batchInsertRolePermissions(any(), any(), any(), any());
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("ADMIN"), "assign-permissions:ADMIN");
	}

	@Test
	@DisplayName("分配权限：权限码无效时抛出 ROLE_PERMISSION_INVALID")
	void assignWhenPermissionInvalidShouldThrow() {
		SysRoleEntity role = new SysRoleEntity();
		role.setRoleCode("ADMIN");
		when(sysRoleMapper.selectOne(any())).thenReturn(role);
		when(sysPermissionMapper.selectCount(any())).thenReturn(0L);

		List<String> permissionCodes = List.of("missing:perm");
		assertThatThrownBy(() -> sysRolePermissionService.assignPermissions("ADMIN", permissionCodes))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.ROLE_PERMISSION_INVALID);

		verify(sysRolePermissionMapper).deleteByRoleCode("ADMIN");
		verify(sysRolePermissionMapper, never()).batchInsertRolePermissions(any(), any(), any(), any());
	}

}
