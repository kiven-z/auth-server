package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.mapper.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

	@InjectMocks
	private SysRolePermissionServiceImpl sysRolePermissionService;

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
		verify(sysRolePermissionMapper).batchInsertRolePermissions("ADMIN", List.of("sys:user:list"), 0L, 0L);
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("ADMIN"), "assign-permissions:ADMIN");
	}

}
