package com.auth.service.system.admin.service.impl;

import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.admin.mapper.SysMenuPermissionMapper;
import com.auth.service.system.admin.mapper.SysPermissionMapper;
import com.auth.service.system.admin.mapper.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.form.permission.SysPermissionUpdateForm;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundMenuPO;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundRolePO;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.authorization.dispatch.trigger.PermissionAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link SysPermissionServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysPermissionServiceImpl 系统权限")
@ExtendWith(MockitoExtension.class)
class SysPermissionServiceImplTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private PermissionAuthorizationInvalidationTrigger permissionInvalidationTrigger;

	@Mock
	private RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	@Mock
	private SysRolePermissionMapper sysRolePermissionMapper;

	@Mock
	private SysMenuPermissionMapper sysMenuPermissionMapper;

	private SysPermissionServiceImpl sysPermissionService;

	private static SysPermissionEntity permissionEntity(Long id) {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(id);
		entity.setPermissionCode("sys:user:query");
		entity.setPermissionName("测试权限");
		entity.setPermissionType(1);
		entity.setStatus(true);
		entity.setOrderNum(0);
		return entity;
	}

	private static SysPermissionUpdateForm updateForm(Long id, String permissionCode, Boolean status) {
		SysPermissionUpdateForm form = new SysPermissionUpdateForm();
		form.setId(id);
		form.setPermissionCode(permissionCode);
		form.setPermissionName("测试权限");
		form.setStatus(status);
		form.setOrderNum(0);
		return form;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysPermissionService = new SysPermissionServiceImpl(auditUserDisplayService, permissionInvalidationTrigger,
				roleInvalidationTrigger, sysRolePermissionMapper, sysMenuPermissionMapper);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysPermissionService, sysPermissionMapper);
	}

	@Test
	@DisplayName("新增权限：编码已存在时抛出 PERMISSION_CODE_DUPLICATE")
	void createThrowsWhenPermissionCodeExists() {
		SysPermissionCreateForm form = new SysPermissionCreateForm();
		form.setPermissionCode("sys:user:list");
		form.setPermissionName("列表");
		form.setPermissionType(0);
		when(sysPermissionMapper.selectCount(any())).thenReturn(1L);

		assertThatThrownBy(() -> sysPermissionService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);
	}

	@Test
	@DisplayName("更新权限：不存在时抛出 PERMISSION_NOT_FOUND")
	void updateThrowsWhenNotFound() {
		SysPermissionUpdateForm form = updateForm(9L, "sys:user:query", true);
		when(sysPermissionMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPermissionService.update(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(permissionInvalidationTrigger);
	}

	@Test
	@DisplayName("更新权限：编码冲突时抛出 PERMISSION_CODE_DUPLICATE")
	void updateThrowsWhenPermissionCodeDuplicate() {
		SysPermissionUpdateForm form = updateForm(1L, "sys:user:update", true);
		when(sysPermissionMapper.selectById(1L)).thenReturn(permissionEntity(1L));
		when(sysPermissionMapper.selectCount(any())).thenReturn(1L);

		assertThatThrownBy(() -> sysPermissionService.update(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verifyNoInteractions(permissionInvalidationTrigger);
	}

	@Test
	@DisplayName("更新权限：不修改 permission_type")
	void updateShouldNotChangePermissionType() {
		SysPermissionEntity entity = permissionEntity(1L);
		entity.setPermissionType(0);
		SysPermissionUpdateForm form = updateForm(1L, "sys:user:query", true);
		form.setPermissionName("重命名");
		when(sysPermissionMapper.selectById(1L)).thenReturn(entity);

		sysPermissionService.update(form);

		assertThat(entity.getPermissionType()).isZero();
	}

	@Test
	@DisplayName("更新权限：仅名称变更时不触发授权失效")
	void updateShouldNotInvalidateWhenOnlyNameChanged() {
		SysPermissionUpdateForm form = updateForm(2L, "sys:user:query", true);
		form.setPermissionName("用户查询");
		when(sysPermissionMapper.selectById(2L)).thenReturn(permissionEntity(2L));

		sysPermissionService.update(form);

		verify(permissionInvalidationTrigger, never()).submitByPermissionCodes(any(), any());
	}

	@Test
	@DisplayName("更新权限：状态变更时触发授权失效")
	void updateShouldInvalidateWhenStatusChanged() {
		SysPermissionUpdateForm form = updateForm(3L, "sys:user:query", false);
		when(sysPermissionMapper.selectById(3L)).thenReturn(permissionEntity(3L));

		sysPermissionService.update(form);

		verify(permissionInvalidationTrigger).submitByPermissionCodes(List.of("sys:user:query"), "update");
	}

	@Test
	@DisplayName("更新权限：编码重命名时触发授权失效并携带旧新码")
	void updateShouldInvalidateWhenPermissionCodeRenamed() {
		SysPermissionUpdateForm form = updateForm(4L, "sys:user:list", true);
		when(sysPermissionMapper.selectById(4L)).thenReturn(permissionEntity(4L));
		when(sysPermissionMapper.selectCount(any())).thenReturn(0L);

		sysPermissionService.update(form);

		verify(permissionInvalidationTrigger).submitByPermissionCodes(List.of("sys:user:query", "sys:user:list"),
				"update");
	}

	@Test
	@DisplayName("权限详情：不存在时抛出 PERMISSION_NOT_FOUND")
	void getDetailThrowsWhenNotFound() {
		when(sysPermissionMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPermissionService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("权限详情：返回 VO 并填充审计用户名")
	void getDetailReturnsVoAndEnrichesAuditUsernames() {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(3L);
		entity.setPermissionCode("sys:dept:query");
		entity.setPermissionName("部门查询");
		entity.setPermissionType(1);
		entity.setStatus(true);
		when(sysPermissionMapper.selectById(3L)).thenReturn(entity);
		when(sysRolePermissionMapper.selectBoundRolesByPermissionCode("sys:dept:query")).thenReturn(List.of());

		SysPermissionDetailVO detail = sysPermissionService.getDetail(3L);

		assertThat(detail.getPermissionCode()).isEqualTo("sys:dept:query");
		assertThat(detail.getPermissionType()).isEqualTo(1);
		assertThat(detail.getBoundRoles()).isEmpty();
		assertThat(detail.getBoundMenus()).isEmpty();
		verify(sysRolePermissionMapper).selectBoundRolesByPermissionCode("sys:dept:query");
		verify(sysMenuPermissionMapper, never()).selectBoundMenusByPermissionCode(any());
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("权限详情：按钮权限填充角色与菜单引用")
	void getDetailShouldIncludeBoundRolesAndMenusForButtonPermission() {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(7L);
		entity.setPermissionCode("sys:user:create");
		entity.setPermissionName("新增用户");
		entity.setPermissionType(0);
		entity.setStatus(true);
		when(sysPermissionMapper.selectById(7L)).thenReturn(entity);

		SysPermissionBoundRolePO boundRole = new SysPermissionBoundRolePO();
		boundRole.setRoleCode("ADMIN");
		boundRole.setRoleName("管理员");
		boundRole.setStatus(true);
		boundRole.setRoleType(1);
		boundRole.setGrantTime(LocalDateTime.of(2026, 6, 4, 10, 30, 0));
		when(sysRolePermissionMapper.selectBoundRolesByPermissionCode("sys:user:create"))
			.thenReturn(List.of(boundRole));

		SysPermissionBoundMenuPO boundMenu = new SysPermissionBoundMenuPO();
		boundMenu.setId(100L);
		boundMenu.setTitle("用户管理");
		boundMenu.setName("SystemUser");
		boundMenu.setPath("/system/user");
		boundMenu.setMenuType(0);
		boundMenu.setStatus(true);
		when(sysMenuPermissionMapper.selectBoundMenusByPermissionCode("sys:user:create"))
			.thenReturn(List.of(boundMenu));

		SysPermissionDetailVO detail = sysPermissionService.getDetail(7L);

		assertThat(detail.getBoundRoles()).hasSize(1);
		assertThat(detail.getBoundRoles().get(0).getRoleCode()).isEqualTo("ADMIN");
		assertThat(detail.getBoundRoles().get(0).getGrantTime()).isEqualTo(LocalDateTime.of(2026, 6, 4, 10, 30, 0));
		assertThat(detail.getBoundMenus()).hasSize(1);
		assertThat(detail.getBoundMenus().get(0).getTitle()).isEqualTo("用户管理");
		verify(sysMenuPermissionMapper).selectBoundMenusByPermissionCode("sys:user:create");
	}

	@Test
	@DisplayName("删除权限：不存在时抛出 PERMISSION_NOT_FOUND")
	void deleteThrowsWhenNotFound() {
		when(sysPermissionMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPermissionService.deleteById(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verify(sysPermissionMapper, never()).deleteById(any());
	}

	@Test
	@DisplayName("删除权限：删除前快照角色并提交 ROLE 失效，再物理删除")
	void deleteShouldInvalidateBoundRolesBeforePhysicalDelete() {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(5L);
		entity.setPermissionCode("sys:role:delete");
		when(sysPermissionMapper.selectById(5L)).thenReturn(entity);
		SysPermissionBoundRolePO boundRole = new SysPermissionBoundRolePO();
		boundRole.setRoleCode("TEST");
		when(sysRolePermissionMapper.selectBoundRolesByPermissionCode("sys:role:delete"))
			.thenReturn(List.of(boundRole));

		sysPermissionService.deleteById(5L);

		InOrder order = Mockito.inOrder(permissionInvalidationTrigger, roleInvalidationTrigger, sysPermissionMapper);
		order.verify(permissionInvalidationTrigger).submitByPermissionCodes(List.of("sys:role:delete"), "delete");
		order.verify(roleInvalidationTrigger).submitByRoleCodes(List.of("TEST"), "delete-permission");
		order.verify(sysPermissionMapper).deleteById(5L);
	}

	@Test
	@DisplayName("删除权限：无绑定角色时不提交 ROLE 失效")
	void deleteShouldSkipRoleInvalidationWhenNoBoundRoles() {
		SysPermissionEntity entity = new SysPermissionEntity();
		entity.setId(6L);
		entity.setPermissionCode("sys:orphan:perm");
		when(sysPermissionMapper.selectById(6L)).thenReturn(entity);
		when(sysRolePermissionMapper.selectBoundRolesByPermissionCode("sys:orphan:perm")).thenReturn(List.of());

		sysPermissionService.deleteById(6L);

		verify(permissionInvalidationTrigger).submitByPermissionCodes(List.of("sys:orphan:perm"), "delete");
		verify(roleInvalidationTrigger, never()).submitByRoleCodes(any(), any());
		verify(sysPermissionMapper).deleteById(6L);
	}

}
