package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.SysRoleCascadeMapper;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.mapper.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleCreateForm;
import com.auth.service.system.admin.model.form.role.SysRoleUpdateForm;
import com.auth.service.system.authorization.dispatch.RoleAuthorizationInvalidationTrigger;
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
import static org.mockito.ArgumentMatchers.argThat;
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
	private GrantTableMapper grantTableMapper;

	@Mock
	private SysRolePermissionMapper sysRolePermissionMapper;

	@Mock
	private SysRoleCascadeMapper sysRoleCascadeMapper;

	@Mock
	private RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	private SysRoleServiceImpl sysRoleService;

	private static SysRoleCreateForm minimalForm() {
		SysRoleCreateForm form = new SysRoleCreateForm();
		form.setRoleCode("CUSTOM_R_ONE");
		form.setRoleName("自定义角色");
		form.setStatus(true);
		form.setRoleType(4);
		return form;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysRoleService = new SysRoleServiceImpl(auditUserDisplayService, grantTableMapper, sysRolePermissionMapper,
				sysRoleCascadeMapper, roleInvalidationTrigger);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysRoleService, sysRoleMapper);
	}

	@Test
	@DisplayName("新增角色：角色编码已存在时抛出 ROLE_CODE_DUPLICATE")
	void createThrowsWhenRoleCodeExists() {
		SysRoleCreateForm form = minimalForm();
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatThrownBy(() -> sysRoleService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.ROLE_CODE_DUPLICATE);
	}

	@Test
	@DisplayName("新增角色：编码去空格后持久化")
	void createTrimsRoleCodeAndPersists() {
		SysRoleCreateForm form = minimalForm();
		form.setRoleCode("  CUSTOM_R_ONE  ");
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);
		when(sysRoleMapper.insert(any(SysRoleEntity.class))).thenReturn(1);

		sysRoleService.create(form);

		verify(sysRoleMapper).insert(argThat(
				(SysRoleEntity e) -> "CUSTOM_R_ONE".equals(e.getRoleCode()) && Boolean.TRUE.equals(e.getStatus())));
	}

	@Test
	@DisplayName("更新角色：仅改名称不触发失效")
	void updateDisplayOnlyShouldNotInvalidate() {
		SysRoleEntity existing = roleEntity(1L, "ADMIN", "旧名", true);
		when(sysRoleMapper.selectById(1L)).thenReturn(existing);
		when(sysRoleMapper.updateById(any(SysRoleEntity.class))).thenReturn(1);

		SysRoleUpdateForm form = updateForm(1L, "ADMIN", "新名", true);
		sysRoleService.update(form);

		verify(sysRoleMapper, never()).selectCount(any());
		verify(roleInvalidationTrigger, never()).submitByRoleCodes(any(), any());
	}

	@Test
	@DisplayName("更新角色：改为已存在的编码时抛出 ROLE_CODE_DUPLICATE")
	void updateThrowsWhenNewRoleCodeDuplicate() {
		SysRoleEntity existing = roleEntity(1L, "ADMIN", "管理员", true);
		when(sysRoleMapper.selectById(1L)).thenReturn(existing);
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		SysRoleUpdateForm form = updateForm(1L, "DUPLICATE", "管理员", true);

		assertThatThrownBy(() -> sysRoleService.update(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.ROLE_CODE_DUPLICATE);

		verify(sysRoleMapper, never()).updateById(any(SysRoleEntity.class));
		verifyNoInteractions(roleInvalidationTrigger);
	}

	@Test
	@DisplayName("更新角色：status 变化时触发失效")
	void updateStatusChangeShouldInvalidate() {
		SysRoleEntity existing = roleEntity(1L, "ADMIN", "管理员", true);
		when(sysRoleMapper.selectById(1L)).thenReturn(existing);
		when(sysRoleMapper.updateById(any(SysRoleEntity.class))).thenReturn(1);

		SysRoleUpdateForm form = updateForm(1L, "ADMIN", "管理员", false);
		sysRoleService.update(form);

		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("ADMIN"), "update:ADMIN");
	}

	@Test
	@DisplayName("删除角色：grant 引用存在时抛出 ROLE_IN_USE")
	void deleteWhenGrantInUseShouldThrow() {
		SysRoleEntity existing = roleEntity(2L, "CUSTOM", "自定义", true);
		when(sysRoleMapper.selectById(2L)).thenReturn(existing);
		when(grantTableMapper.countByRoleCode("CUSTOM")).thenReturn(1L);

		assertThatThrownBy(() -> sysRoleService.deleteById(2L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.ROLE_IN_USE);
	}

	@Test
	@DisplayName("删除角色：无 grant 引用时清理关联并触发失效")
	void deleteWithoutGrantShouldCleanupAndInvalidate() {
		SysRoleEntity existing = roleEntity(3L, "EMPTY_ROLE", "空角色", true);
		when(sysRoleMapper.selectById(3L)).thenReturn(existing);
		when(grantTableMapper.countByRoleCode("EMPTY_ROLE")).thenReturn(0L);

		sysRoleService.deleteById(3L);

		verify(sysRolePermissionMapper).deleteByRoleCode("EMPTY_ROLE");
		verify(sysRoleCascadeMapper).deleteMenuRolesByRoleCode("EMPTY_ROLE");
		verify(sysRoleMapper).deleteById(3L);

		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("EMPTY_ROLE"), "delete:EMPTY_ROLE");
	}

	private static SysRoleEntity roleEntity(Long id, String roleCode, String roleName, boolean status) {
		SysRoleEntity entity = new SysRoleEntity();
		entity.setId(id);
		entity.setRoleCode(roleCode);
		entity.setRoleName(roleName);
		entity.setStatus(status);
		entity.setRoleType(4);
		return entity;
	}

	private static SysRoleUpdateForm updateForm(Long id, String roleCode, String roleName, boolean status) {
		SysRoleUpdateForm form = new SysRoleUpdateForm();
		form.setId(id);
		form.setRoleCode(roleCode);
		form.setRoleName(roleName);
		form.setStatus(status);
		form.setRoleType(4);
		return form;
	}

}
