package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.service.system.admin.mapper.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.mapper.role.SysRolePermissionMapper;
import com.auth.service.system.admin.model.constants.AdminBatchSizes;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundMenuPO;
import com.auth.service.system.admin.model.po.reference.*;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
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
		sysRoleService = spy(new SysRoleServiceImpl(auditUserDisplayService, roleInvalidationTrigger, grantTableMapper,
				sysRolePermissionMapper, sysMenuRoleMapper));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysRoleService, sysRoleMapper);
		lenient().doReturn(true).when(sysRoleService).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增角色：空列表直接返回")
	void createBatchFromImportSkipsWhenEmpty() {
		sysRoleService.createBatchFromImport(List.of());

		verify(sysRoleMapper, never()).selectReferenceByRoleCodes(anyList());
		verify(sysRoleService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增角色：校验通过后批量落库")
	void createBatchFromImportPersistsEntities() {
		SysRoleForm form = new SysRoleForm();
		form.setRoleCode("AUDITOR");
		form.setRoleName("审计员");
		form.setStatus(true);
		when(sysRoleMapper.selectReferenceByRoleCodes(List.of("AUDITOR"))).thenReturn(List.of());

		sysRoleService.createBatchFromImport(List.of(form));

		verify(sysRoleMapper).selectReferenceByRoleCodes(List.of("AUDITOR"));
		verify(sysRoleService).saveBatch(anyList(), eq(AdminBatchSizes.IMPORT_SAVE));
	}

	@Test
	@DisplayName("批量新增角色：请求内编码重复时抛出 DATA_CODE_DUPLICATE")
	void createBatchFromImportThrowsWhenDuplicatedInRequest() {
		SysRoleForm form1 = new SysRoleForm();
		form1.setRoleCode("ADMIN");
		form1.setRoleName("管理员");
		SysRoleForm form2 = new SysRoleForm();
		form2.setRoleCode("ADMIN");
		form2.setRoleName("管理员2");

		ThrowingCallable executable = () -> sysRoleService.createBatchFromImport(List.of(form1, form2));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysRoleMapper, never()).selectReferenceByRoleCodes(anyList());
		verify(sysRoleService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("批量新增角色：编码已存在时抛出 DATA_CODE_DUPLICATE")
	void createBatchFromImportThrowsWhenRoleCodeExists() {
		SysRoleForm form = new SysRoleForm();
		form.setRoleCode("ADMIN");
		form.setRoleName("管理员");
		RoleReferencePO existing = new RoleReferencePO();
		existing.setRoleCode("ADMIN");
		when(sysRoleMapper.selectReferenceByRoleCodes(List.of("ADMIN"))).thenReturn(List.of(existing));

		ThrowingCallable executable = () -> sysRoleService.createBatchFromImport(List.of(form));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysRoleService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("角色详情：不存在时抛出 ROLE_NOT_FOUND")
	void getDetailThrowsWhenNotFound() {
		when(sysRoleMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysRoleService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(sysRolePermissionMapper, sysMenuRoleMapper);
	}

	@Test
	@DisplayName("角色详情：返回已分配权限列表")
	void getDetailShouldIncludeBoundPermissions() {
		when(sysRoleMapper.selectById(1L)).thenReturn(roleEntity(1L, "ADMIN"));

		PermissionReferencePO permission = new PermissionReferencePO();
		permission.setId(100L);
		permission.setPermissionCode("sys:user:query");
		permission.setPermissionName("用户查询");
		permission.setStatus(true);
		when(sysRolePermissionMapper.selectAssignedPermissionsByRoleId(1L)).thenReturn(List.of(permission));
		when(sysMenuRoleMapper.selectBoundMenusByRoleId(1L)).thenReturn(List.of());
		when(grantTableMapper.selectBoundUsersByRoleId(1L)).thenReturn(List.of());
		when(grantTableMapper.selectBoundDeptsByRoleId(1L)).thenReturn(List.of());
		when(grantTableMapper.selectBoundPostsByRoleId(1L)).thenReturn(List.of());

		SysRoleDetailVO detail = sysRoleService.getDetail(1L);

		assertThat(detail.getRoleCode()).isEqualTo("ADMIN");
		assertThat(detail.getBoundPermissions()).hasSize(1);
		assertThat(detail.getBoundPermissions().get(0).getId()).isEqualTo(100L);
		assertThat(detail.getBoundPermissions().get(0).getPermissionCode()).isEqualTo("sys:user:query");
		verify(sysRolePermissionMapper).selectAssignedPermissionsByRoleId(1L);
		verify(sysMenuRoleMapper).selectBoundMenusByRoleId(1L);
		verify(grantTableMapper).selectBoundUsersByRoleId(1L);
		verify(grantTableMapper).selectBoundDeptsByRoleId(1L);
		verify(grantTableMapper).selectBoundPostsByRoleId(1L);
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
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

		UserReferencePO boundUser = new UserReferencePO();
		boundUser.setId(100L);
		boundUser.setUsername("alice");
		boundUser.setNickname("Alice");
		boundUser.setEmployeeNo("E001");
		boundUser.setStatus(1);
		when(grantTableMapper.selectBoundUsersByRoleId(2L)).thenReturn(List.of(boundUser));

		DeptReferencePO boundDept = new DeptReferencePO();
		boundDept.setId(20L);
		boundDept.setDeptCode("RD");
		boundDept.setDeptName("研发部");
		boundDept.setStatus(true);
		when(grantTableMapper.selectBoundDeptsByRoleId(2L)).thenReturn(List.of(boundDept));

		PostReferencePO boundPost = new PostReferencePO();
		boundPost.setId(30L);
		boundPost.setPostCode("DEV");
		boundPost.setPostName("开发岗");
		boundPost.setStatus(true);
		when(grantTableMapper.selectBoundPostsByRoleId(2L)).thenReturn(List.of(boundPost));

		when(sysRolePermissionMapper.selectAssignedPermissionsByRoleId(2L)).thenReturn(List.of());

		SysRoleDetailVO detail = sysRoleService.getDetail(2L);

		assertThat(detail.getBoundMenus()).hasSize(1);
		assertThat(detail.getBoundMenus().get(0).getTitle()).isEqualTo("用户管理");
		assertThat(detail.getBoundUsers()).hasSize(1);
		assertThat(detail.getBoundUsers().get(0).getId()).isEqualTo(100L);
		assertThat(detail.getBoundUsers().get(0).getUsername()).isEqualTo("alice");
		assertThat(detail.getBoundDepts()).hasSize(1);
		assertThat(detail.getBoundDepts().get(0).getId()).isEqualTo(20L);
		assertThat(detail.getBoundDepts().get(0).getDeptCode()).isEqualTo("RD");
		assertThat(detail.getBoundPosts()).hasSize(1);
		assertThat(detail.getBoundPosts().get(0).getId()).isEqualTo(30L);
		assertThat(detail.getBoundPosts().get(0).getPostCode()).isEqualTo("DEV");

		verify(sysMenuRoleMapper).selectBoundMenusByRoleId(2L);
		verify(grantTableMapper).selectBoundUsersByRoleId(2L);
		verify(grantTableMapper).selectBoundDeptsByRoleId(2L);
		verify(grantTableMapper).selectBoundPostsByRoleId(2L);
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

	@Test
	@DisplayName("批量启停：按存在角色更新并按编码失效")
	void batchUpdateStatusUpdatesAndInvalidatesByRoleCodes() {
		doReturn(List.of(roleEntity(1L, "R1"), roleEntity(2L, "R2"))).when(sysRoleService).listByIds(any());
		doReturn(true).when(sysRoleService).updateBatchById(anyList());

		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(List.of(1L, 2L));
		form.setStatus(false);
		sysRoleService.batchUpdateStatus(form);

		verify(sysRoleService).updateBatchById(anyList());
		verify(roleInvalidationTrigger).submitByRoleCodes(List.of("R1", "R2"), "update");
	}

	@Test
	@DisplayName("批量启停：ids 为空时不写库")
	void batchUpdateStatusSkipsWhenIdsEmpty() {
		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(List.of());
		form.setStatus(true);
		sysRoleService.batchUpdateStatus(form);

		verify(sysRoleService, never()).listByIds(any());
		verify(sysRoleService, never()).updateBatchById(anyList());
		verifyNoInteractions(roleInvalidationTrigger);
	}

}
