package com.auth.service.system.admin.service.impl;

import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.mapper.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.po.role.SysRoleAssignedPermissionPO;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
	private RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	@Mock
	private GrantTableMapper grantTableMapper;

	@Mock
	private SysRolePermissionMapper sysRolePermissionMapper;

	private SysRoleServiceImpl sysRoleService;

	private static SysRoleForm updateForm(Long id, String roleCode, boolean status) {
		SysRoleForm form = new SysRoleForm();
		form.setId(id);
		form.setRoleCode(roleCode);
		form.setRoleName("测试角色");
		form.setStatus(status);
		form.setRoleType(4);
		form.setOrderNum(0);
		return form;
	}

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
		sysRoleService = new SysRoleServiceImpl(auditUserDisplayService, roleInvalidationTrigger, grantTableMapper,
				sysRolePermissionMapper);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysRoleService, sysRoleMapper);
	}

	@Test
	@DisplayName("角色详情：不存在时抛出 ROLE_NOT_FOUND")
	void getDetailThrowsWhenNotFound() {
		when(sysRoleMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysRoleService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(sysRolePermissionMapper);
	}

	@Test
	@DisplayName("角色详情：返回已分配权限列表")
	void getDetailShouldIncludeBoundPermissions() {
		when(sysRoleMapper.selectById(1L)).thenReturn(roleEntity(1L, "ADMIN"));

		SysRoleAssignedPermissionPO permission = new SysRoleAssignedPermissionPO();
		permission.setPermissionCode("sys:user:query");
		permission.setPermissionName("用户查询");
		permission.setPermissionType(1);
		permission.setStatus(true);
		permission.setCreateTime(LocalDateTime.of(2026, 6, 4, 11, 0, 0));
		permission.setUpdateTime(LocalDateTime.of(2026, 6, 4, 11, 0, 0));
		when(sysRolePermissionMapper.selectAssignedPermissionsByRoleCode("ADMIN")).thenReturn(List.of(permission));

		SysRoleDetailVO detail = sysRoleService.getDetail(1L);

		assertThat(detail.getRoleCode()).isEqualTo("ADMIN");
		assertThat(detail.getRoleTypeDesc()).isEqualTo("自定义角色");
		assertThat(detail.getBoundPermissions()).hasSize(1);
		assertThat(detail.getBoundPermissions().get(0).getPermissionCode()).isEqualTo("sys:user:query");
		assertThat(detail.getBoundPermissions().get(0).getPermissionType()).isEqualTo(1);
		assertThat(detail.getBoundPermissions().get(0).getCreateTime())
			.isEqualTo(LocalDateTime.of(2026, 6, 4, 11, 0, 0));
		assertThat(detail.getBoundPermissions().get(0).getUpdateTime())
			.isEqualTo(LocalDateTime.of(2026, 6, 4, 11, 0, 0));
		verify(sysRolePermissionMapper).selectAssignedPermissionsByRoleCode("ADMIN");
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("更新角色：仅改名称时不提交授权失效")
	void updateWithoutAuthImpactSkipsInvalidation() {
		when(sysRoleMapper.selectById(4L)).thenReturn(roleEntity(4L, "EDITOR"));
		when(sysRoleMapper.updateById(any(SysRoleEntity.class))).thenReturn(1);

		SysRoleForm form = updateForm(4L, "EDITOR", true);
		form.setRoleName("编辑者");

		sysRoleService.update(form);

		verify(sysRoleMapper).updateById(argThat((SysRoleEntity e) -> "编辑者".equals(e.getRoleName())));
		verifyNoInteractions(roleInvalidationTrigger);
	}

	@Test
	@DisplayName("更新角色：状态变更时提交 ROLE 授权失效")
	void updateWithStatusChangeSubmitsInvalidation() {
		when(sysRoleMapper.selectById(5L)).thenReturn(roleEntity(5L, "VIEWER"));
		when(sysRoleMapper.updateById(any(SysRoleEntity.class))).thenReturn(1);

		sysRoleService.update(updateForm(5L, "VIEWER", false));

		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("VIEWER"), "update");
	}

	@Test
	@DisplayName("更新角色：编码重命名时提交失效并携带旧新码")
	void updateWithRoleCodeRenameSubmitsInvalidation() {
		when(sysRoleMapper.selectById(6L)).thenReturn(roleEntity(6L, "OLD_CODE"));
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);
		when(sysRoleMapper.updateById(any(SysRoleEntity.class))).thenReturn(1);

		sysRoleService.update(updateForm(6L, "NEW_CODE", true));

		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("OLD_CODE", "NEW_CODE"), "update");
	}

	@Test
	@DisplayName("删除角色：不存在时抛出 ROLE_NOT_FOUND")
	void deleteThrowsWhenNotFound() {
		when(sysRoleMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysRoleService.deleteById(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

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
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

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
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("EMPTY_ROLE"), "delete");
	}

}
