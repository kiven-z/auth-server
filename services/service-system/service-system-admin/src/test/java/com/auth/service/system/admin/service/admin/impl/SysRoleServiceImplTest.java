package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.mapper.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.mapper.role.SysRolePermissionMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundDeptPO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundPostPO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundUserPO;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundMenuPO;
import com.auth.service.system.admin.model.po.role.SysRoleAssignedPermissionPO;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

	@Mock
	private SysMenuRoleMapper sysMenuRoleMapper;

	private SysRoleServiceImpl sysRoleService;

	private SysRoleQueryServiceImpl sysRoleQueryService;

	private static SysRoleForm updateForm(Long id, String roleCode, boolean status) {
		SysRoleForm form = new SysRoleForm();
		form.setId(id);
		form.setRoleCode(roleCode);
		form.setRoleName("测试角色");
		form.setStatus(status);
		form.setOrderNum(0);
		return form;
	}

	private static SysRoleEntity roleEntity(Long id, String roleCode) {
		SysRoleEntity entity = new SysRoleEntity();
		entity.setId(id);
		entity.setRoleCode(roleCode);
		entity.setRoleName("测试角色");
		entity.setStatus(true);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysRoleService = new SysRoleServiceImpl(roleInvalidationTrigger, grantTableMapper);
		sysRoleQueryService = new SysRoleQueryServiceImpl(auditUserDisplayService, grantTableMapper,
				sysRolePermissionMapper, sysMenuRoleMapper);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysRoleService, sysRoleMapper);
		baseMapperField.set(sysRoleQueryService, sysRoleMapper);
	}

	@Test
	@DisplayName("角色详情：不存在时抛出 ROLE_NOT_FOUND")
	void getDetailThrowsWhenNotFound() {
		when(sysRoleMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysRoleQueryService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(sysRolePermissionMapper, sysMenuRoleMapper);
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
		when(sysRolePermissionMapper.selectAssignedPermissionsByRoleId(1L)).thenReturn(List.of(permission));
		when(sysMenuRoleMapper.selectBoundMenusByRoleId(1L)).thenReturn(List.of());
		when(grantTableMapper.selectBoundUsersByRoleCode("ADMIN")).thenReturn(List.of());
		when(grantTableMapper.selectBoundDeptsByRoleCode("ADMIN")).thenReturn(List.of());
		when(grantTableMapper.selectBoundPostsByRoleCode("ADMIN")).thenReturn(List.of());

		SysRoleDetailVO detail = sysRoleQueryService.getDetail(1L);

		assertThat(detail.getRoleCode()).isEqualTo("ADMIN");
		assertThat(detail.getBoundPermissions()).hasSize(1);
		assertThat(detail.getBoundPermissions().get(0).getPermissionCode()).isEqualTo("sys:user:query");
		assertThat(detail.getBoundPermissions().get(0).getPermissionType()).isEqualTo(1);
		assertThat(detail.getBoundPermissions().get(0).getCreateTime())
			.isEqualTo(LocalDateTime.of(2026, 6, 4, 11, 0, 0));
		assertThat(detail.getBoundPermissions().get(0).getUpdateTime())
			.isEqualTo(LocalDateTime.of(2026, 6, 4, 11, 0, 0));
		verify(sysRolePermissionMapper).selectAssignedPermissionsByRoleId(1L);
		verify(sysMenuRoleMapper).selectBoundMenusByRoleId(1L);
		verify(grantTableMapper).selectBoundUsersByRoleCode("ADMIN");
		verify(grantTableMapper).selectBoundDeptsByRoleCode("ADMIN");
		verify(grantTableMapper).selectBoundPostsByRoleCode("ADMIN");
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("角色详情：反查并填充已绑定菜单与 grant 授权主体")
	void getDetailShouldIncludeBoundMenusAndGrantSubjects() {
		when(sysRoleMapper.selectById(2L)).thenReturn(roleEntity(2L, "EDITOR"));

		SysPermissionBoundMenuPO boundMenu = new SysPermissionBoundMenuPO();
		boundMenu.setId(10L);
		boundMenu.setTitle("用户管理");
		boundMenu.setName("SystemUser");
		boundMenu.setPath("/system/user");
		boundMenu.setMenuType(0);
		boundMenu.setStatus(true);
		when(sysMenuRoleMapper.selectBoundMenusByRoleId(2L)).thenReturn(List.of(boundMenu));

		GrantTableBoundUserPO boundUser = new GrantTableBoundUserPO();
		boundUser.setUserId(100L);
		boundUser.setUsername("alice");
		boundUser.setNickname("Alice");
		boundUser.setEmployeeNo("E001");
		boundUser.setUserStatus(1);
		boundUser.setCreateTime(LocalDateTime.of(2026, 6, 4, 9, 0, 0));
		boundUser.setUpdateTime(LocalDateTime.of(2026, 6, 4, 9, 30, 0));
		when(grantTableMapper.selectBoundUsersByRoleCode("EDITOR")).thenReturn(List.of(boundUser));

		GrantTableBoundDeptPO boundDept = new GrantTableBoundDeptPO();
		boundDept.setDeptId(20L);
		boundDept.setDeptCode("RD");
		boundDept.setDeptName("研发部");
		boundDept.setStatus(true);
		boundDept.setCreateTime(LocalDateTime.of(2026, 6, 4, 10, 0, 0));
		boundDept.setUpdateTime(LocalDateTime.of(2026, 6, 4, 10, 0, 0));
		when(grantTableMapper.selectBoundDeptsByRoleCode("EDITOR")).thenReturn(List.of(boundDept));

		GrantTableBoundPostPO boundPost = new GrantTableBoundPostPO();
		boundPost.setPostId(30L);
		boundPost.setPostCode("DEV");
		boundPost.setPostName("开发岗");
		boundPost.setStatus(true);
		boundPost.setDeptId(20L);
		boundPost.setDeptName("研发部");
		boundPost.setCreateTime(LocalDateTime.of(2026, 6, 4, 11, 0, 0));
		boundPost.setUpdateTime(LocalDateTime.of(2026, 6, 4, 11, 0, 0));
		when(grantTableMapper.selectBoundPostsByRoleCode("EDITOR")).thenReturn(List.of(boundPost));

		when(sysRolePermissionMapper.selectAssignedPermissionsByRoleId(2L)).thenReturn(List.of());

		SysRoleDetailVO detail = sysRoleQueryService.getDetail(2L);

		assertThat(detail.getBoundMenus()).hasSize(1);
		assertThat(detail.getBoundMenus().get(0).getTitle()).isEqualTo("用户管理");
		assertThat(detail.getBoundUsers()).hasSize(1);
		assertThat(detail.getBoundUsers().get(0).getUsername()).isEqualTo("alice");
		assertThat(detail.getBoundDepts()).hasSize(1);
		assertThat(detail.getBoundDepts().get(0).getDeptCode()).isEqualTo("RD");
		assertThat(detail.getBoundPosts()).hasSize(1);
		assertThat(detail.getBoundPosts().get(0).getPostCode()).isEqualTo("DEV");
		assertThat(detail.getBoundPosts().get(0).getDeptName()).isEqualTo("研发部");

		verify(sysMenuRoleMapper).selectBoundMenusByRoleId(2L);
		verify(grantTableMapper).selectBoundUsersByRoleCode("EDITOR");
		verify(grantTableMapper).selectBoundDeptsByRoleCode("EDITOR");
		verify(grantTableMapper).selectBoundPostsByRoleCode("EDITOR");
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
		when(grantTableMapper.countByRoleId(2L)).thenReturn(1L);

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
		when(grantTableMapper.countByRoleId(3L)).thenReturn(0L);
		when(sysRoleMapper.deleteById(3L)).thenReturn(1);

		sysRoleService.deleteById(3L);

		verify(sysRoleMapper).deleteById(3L);
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("EMPTY_ROLE"), "delete");
	}

}
