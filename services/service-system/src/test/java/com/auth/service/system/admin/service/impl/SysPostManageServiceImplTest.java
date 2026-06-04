package com.auth.service.system.admin.service.impl;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.service.GrantTableSubjectRoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysPostManageServiceImpl} 单元测试
 */
@DisplayName("SysPostManageServiceImpl 岗位角色授权委托")
@ExtendWith(MockitoExtension.class)
class SysPostManageServiceImplTest {

	private static final Long POST_ID = 10L;

	@Mock
	private GrantTableSubjectRoleService grantTableSubjectRoleService;

	@InjectMocks
	private SysPostManageServiceImpl sysPostManageService;

	@Test
	@DisplayName("查询已分配角色：委托 GrantTableSubjectRoleService")
	void listAssignedRolesDelegatesToGrantTableSubjectRoleService() {
		GrantTableAssignedRoleRow row = new GrantTableAssignedRoleRow();
		row.setRoleCode("CUSTOM_A");
		when(grantTableSubjectRoleService.listAssignedRoles(GrantTableSubjectType.POST, POST_ID))
			.thenReturn(List.of(row));

		List<GrantTableAssignedRoleRow> rows = sysPostManageService.listAssignedRoles(POST_ID);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("CUSTOM_A");
		verify(grantTableSubjectRoleService).listAssignedRoles(GrantTableSubjectType.POST, POST_ID);
	}

	@Test
	@DisplayName("全量覆盖：委托 GrantTableSubjectRoleService")
	void replacePostRolesDelegatesToGrantTableSubjectRoleService() {
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of("BIZ_ADMIN"));

		sysPostManageService.replacePostRoles(POST_ID, form);

		verify(grantTableSubjectRoleService).replaceSubjectRoles(GrantTableSubjectType.POST, POST_ID, form);
	}

}
