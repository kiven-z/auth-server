package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link SysRoleServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysRoleServiceImpl 系统角色")
@ExtendWith(MockitoExtension.class)
class SysRoleServiceImplTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	@Mock
	private GrantTableMapper grantTableMapper;

	private SysRoleServiceImpl sysRoleService;

	private static SysRoleEntity roleEntity(Long id, String roleCode) {
		SysRoleEntity entity = new SysRoleEntity();
		entity.setId(id);
		entity.setRoleCode(roleCode);
		entity.setRoleName("测试角色");
		entity.setStatus(true);
		entity.setRoleType(4);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysRoleService = new SysRoleServiceImpl(auditUserDisplayService, roleInvalidationTrigger, grantTableMapper);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysRoleService, sysRoleMapper);
	}

	@Test
	@DisplayName("删除角色：不存在时抛出 ROLE_NOT_FOUND")
	void deleteThrowsWhenNotFound() {
		when(sysRoleMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysRoleService.deleteById(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.ROLE_NOT_FOUND);

		verify(sysRoleMapper, never()).deleteById(any());
		verifyNoInteractions(roleInvalidationTrigger);
	}

	@Test
	@DisplayName("删除角色：grant_table 引用存在时抛出 ROLE_IN_USE")
	void deleteThrowsWhenGrantInUse() {
		when(sysRoleMapper.selectById(2L)).thenReturn(roleEntity(2L, "CUSTOM"));
		when(grantTableMapper.countByRoleCode("CUSTOM")).thenReturn(1L);

		assertThatThrownBy(() -> sysRoleService.deleteById(2L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.ROLE_IN_USE);

		verify(sysRoleMapper, never()).deleteById(any());
		verifyNoInteractions(roleInvalidationTrigger);
	}

	@Test
	@DisplayName("删除角色：物理删除主表并触发授权失效（子表由数据库 CASCADE 维护）")
	void deleteShouldPhysicallyDeleteAndInvalidate() {
		when(sysRoleMapper.selectById(3L)).thenReturn(roleEntity(3L, "EMPTY_ROLE"));
		when(grantTableMapper.countByRoleCode("EMPTY_ROLE")).thenReturn(0L);
		when(sysRoleMapper.deleteById(3L)).thenReturn(1);

		sysRoleService.deleteById(3L);

		verify(sysRoleMapper).deleteById(3L);
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("EMPTY_ROLE"), "delete:EMPTY_ROLE");
	}

}
