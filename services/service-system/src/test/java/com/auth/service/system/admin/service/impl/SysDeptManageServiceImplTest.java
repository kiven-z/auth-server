package com.auth.service.system.admin.service.impl;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.SysDeptDetailMapper;
import com.auth.service.system.admin.model.form.dept.SysDeptAssignRoleForm;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundRolePO;
import com.auth.service.system.admin.model.vo.dept.SysDeptAssignedRoleRow;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.GrantAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
 * {@link SysDeptManageServiceImpl} 单元测试。
 */
@DisplayName("SysDeptManageServiceImpl 部门角色分配")
@ExtendWith(MockitoExtension.class)
class SysDeptManageServiceImplTest {

	@Mock
	private DeptReferenceChecker deptReferenceChecker;

	@Mock
	private SysDeptDetailMapper sysDeptDetailMapper;

	@Mock
	private GrantTableMapper grantTableMapper;

	@Mock
	private RoleGrantReferenceChecker roleGrantReferenceChecker;

	@Mock
	private GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	@InjectMocks
	private SysDeptManageServiceImpl sysDeptManageService;

	@Test
	@DisplayName("查询已分配角色：返回 roleCode 与 roleName")
	void listAssignedRolesReturnsRows() {
		Long deptId = 10L;
		SysDeptBoundRolePO po = new SysDeptBoundRolePO();
		po.setRoleCode("BIZ_ADMIN");
		po.setRoleName("业务管理员");
		po.setRoleType(3);
		when(sysDeptDetailMapper.selectBoundRolesByDeptId(deptId)).thenReturn(List.of(po));

		List<SysDeptAssignedRoleRow> rows = sysDeptManageService.listAssignedRoles(deptId);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("BIZ_ADMIN");
		assertThat(rows.get(0).getRoleName()).isEqualTo("业务管理员");
		verify(deptReferenceChecker).getExistingActive(deptId);
	}

	@Test
	@DisplayName("覆盖分配：删除旧 grant 后批量插入并触发 GRANT 失效")
	void replaceDeptRolesReplacesGrantsAndInvalidates() {
		Long deptId = 10L;
		SysDeptAssignRoleForm form = new SysDeptAssignRoleForm();
		form.setRoleCodes(List.of("BIZ_ADMIN", "CUSTOM_A"));

		sysDeptManageService.replaceDeptRoles(deptId, form);

		verify(roleGrantReferenceChecker).requireDeptGrantEligibleRoles(List.of("BIZ_ADMIN", "CUSTOM_A"));
		verify(grantTableMapper).deleteBySubject(GrantTableSubjectType.DEPT.name(), deptId);
		verify(grantTableMapper).batchInsertSubjectRoleGrants(eq(GrantTableSubjectType.DEPT.name()), eq(deptId),
				eq(List.of("BIZ_ADMIN", "CUSTOM_A")), any());
		verify(grantInvalidationTrigger).submitByGrantSubjects(
				eq(List.of(new GrantSubjectKey(GrantTableSubjectType.DEPT, deptId))), eq("replace-dept-roles:10"));
	}

	@Test
	@DisplayName("覆盖分配：空列表仅删除 grant 不插入")
	void replaceDeptRolesWithEmptyListDeletesOnly() {
		Long deptId = 10L;
		SysDeptAssignRoleForm form = new SysDeptAssignRoleForm();
		form.setRoleCodes(List.of());

		sysDeptManageService.replaceDeptRoles(deptId, form);

		verify(roleGrantReferenceChecker).requireDeptGrantEligibleRoles(List.of());
		verify(grantTableMapper).deleteBySubject(GrantTableSubjectType.DEPT.name(), deptId);
		verify(grantTableMapper, never()).batchInsertSubjectRoleGrants(any(), any(), any(), any());
		verify(grantInvalidationTrigger).submitByGrantSubjects(any(), eq("replace-dept-roles:10"));
	}

	@Test
	@DisplayName("覆盖分配：角色校验失败时不写 grant")
	void replaceDeptRolesThrowsWhenRoleInvalid() {
		Long deptId = 10L;
		SysDeptAssignRoleForm form = new SysDeptAssignRoleForm();
		form.setRoleCodes(List.of("SYS_ADMIN"));

		doThrow(new SystemBusinessException(SystemAdminResultCode.DEPT_ROLE_INVALID)).when(roleGrantReferenceChecker)
			.requireDeptGrantEligibleRoles(List.of("SYS_ADMIN"));

		assertThatThrownBy(() -> sysDeptManageService.replaceDeptRoles(deptId, form))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.DEPT_ROLE_INVALID);

		verify(grantTableMapper, never()).deleteBySubject(any(), any());
		verify(grantInvalidationTrigger, never()).submitByGrantSubjects(any(), any());
	}

	@Test
	@DisplayName("查询已分配角色：部门不存在时抛出 DEPT_NOT_FOUND")
	void listAssignedRolesThrowsWhenDeptNotFound() {
		Long deptId = 99L;
		when(deptReferenceChecker.getExistingActive(deptId))
			.thenThrow(new SystemBusinessException(SystemAdminResultCode.DEPT_NOT_FOUND, deptId));

		assertThatThrownBy(() -> sysDeptManageService.listAssignedRoles(deptId))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.DEPT_NOT_FOUND);
	}

}
