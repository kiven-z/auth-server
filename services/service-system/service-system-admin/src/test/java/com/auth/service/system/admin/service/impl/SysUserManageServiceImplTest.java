package com.auth.service.system.admin.service.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundRolePO;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.admin.support.user.UserOperationChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserManageServiceImpl} 单元测试
 */
@DisplayName("SysUserManageServiceImpl 用户直连角色授权")
@ExtendWith(MockitoExtension.class)
class SysUserManageServiceImplTest {

	private static final Long USER_ID = 10L;

	@Mock
	private SysUserMapper sysUserMapper;

	@Mock
	private UserOperationChecker userOperationChecker;

	@Mock
	private RoleGrantReferenceChecker roleGrantReferenceChecker;

	@Mock
	private GrantTableMapper grantTableMapper;

	@Mock
	private UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	private SysUserManageServiceImpl sysUserManageService;

	@BeforeEach
	void setUp() {
		UserReferenceChecker userReferenceChecker = new UserReferenceChecker(sysUserMapper);
		sysUserManageService = new SysUserManageServiceImpl(userReferenceChecker, userOperationChecker,
				roleGrantReferenceChecker, grantTableMapper, userAuthorizationInvalidationTrigger);
	}

	@Test
	@DisplayName("查询已分配角色：用户存在时返回转换结果")
	void listAssignedRolesReturnsRowsWhenUserExists() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser(USER_ID));
		GrantTableBoundRolePO boundRole = new GrantTableBoundRolePO();
		boundRole.setRoleCode("CUSTOM_A");
		boundRole.setRoleName("业务角色A");
		when(grantTableMapper.selectBoundRolesBySubject(GrantTableSubjectType.USER.name(), USER_ID,
				RoleTypeEnum.SUBJECT_GRANT_TYPES))
			.thenReturn(List.of(boundRole));

		List<GrantTableAssignedRoleRow> rows = sysUserManageService.listAssignedRoles(USER_ID);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("CUSTOM_A");
		assertThat(rows.get(0).getRoleName()).isEqualTo("业务角色A");
	}

	@Test
	@DisplayName("查询已分配角色：用户不存在时抛出 USER_NOT_FOUND")
	void listAssignedRolesThrowsWhenUserMissing() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(null);

		assertThatThrownBy(() -> sysUserManageService.listAssignedRoles(USER_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("全量覆盖：写入 grant_table 并提交用户授权失效")
	void replaceUserRolesWritesGrantAndSubmitsInvalidation() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser(USER_ID));
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of("BIZ_ADMIN", "CUSTOM_A"));

		sysUserManageService.replaceUserRoles(USER_ID, form);

		verify(userOperationChecker).requireOperable(List.of(USER_ID));
		verify(roleGrantReferenceChecker).requireGrantEligibleRoles(List.of("BIZ_ADMIN", "CUSTOM_A"),
				RoleTypeEnum.SUBJECT_GRANT_TYPES, SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		verify(grantTableMapper).deleteBySubject(GrantTableSubjectType.USER.name(), USER_ID);
		verify(grantTableMapper).batchInsertSubjectRoleGrants(eq(GrantTableSubjectType.USER.name()), eq(USER_ID),
				eq(List.of("BIZ_ADMIN", "CUSTOM_A")), any());
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "replace-roles");
	}

	@Test
	@DisplayName("全量覆盖：空角色列表仅删除 grant 行仍触发用户失效")
	void replaceUserRolesClearsGrantsWhenRoleCodesEmpty() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser(USER_ID));
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of());

		sysUserManageService.replaceUserRoles(USER_ID, form);

		verify(grantTableMapper).deleteBySubject(GrantTableSubjectType.USER.name(), USER_ID);
		verify(grantTableMapper, never()).batchInsertSubjectRoleGrants(any(), any(), any(), any());
		verify(userAuthorizationInvalidationTrigger).submitByUserIds(List.of(USER_ID), "replace-roles");
	}

	@Test
	@DisplayName("全量覆盖：受保护用户不允许分配角色")
	void replaceUserRolesThrowsWhenUserNotOperable() {
		when(sysUserMapper.selectById(USER_ID)).thenReturn(activeUser(USER_ID));
		doThrow(new SystemBusinessException(SystemAdminResultCode.USER_OPERATION_FORBIDDEN, USER_ID))
			.when(userOperationChecker)
			.requireOperable(List.of(USER_ID));
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of("CUSTOM_A"));

		assertThatThrownBy(() -> sysUserManageService.replaceUserRoles(USER_ID, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);

		verify(grantTableMapper, never()).deleteBySubject(any(), any());
		verify(userAuthorizationInvalidationTrigger, never()).submitByUserIds(any(), any());
	}

	private static UserEntity activeUser(Long id) {
		UserEntity entity = new UserEntity();
		entity.setId(id);
		entity.setUsername("tester");
		return entity;
	}

}
