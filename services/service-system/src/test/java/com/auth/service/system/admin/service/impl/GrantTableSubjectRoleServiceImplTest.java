package com.auth.service.system.admin.service.impl;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.PostReferenceMapper;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundRolePO;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.support.granttable.GrantTableSubjectExistenceVerifier;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.GrantAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link GrantTableSubjectRoleServiceImpl} 单元测试
 */
@DisplayName("GrantTableSubjectRoleServiceImpl grant_table 主体角色授权")
@ExtendWith(MockitoExtension.class)
class GrantTableSubjectRoleServiceImplTest {

	private static final Long SUBJECT_ID = 10L;

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private PostReferenceMapper postReferenceMapper;

	@Mock
	private GrantTableMapper grantTableMapper;

	@Mock
	private RoleGrantReferenceChecker roleGrantReferenceChecker;

	@Mock
	private GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	private GrantTableSubjectRoleServiceImpl grantTableSubjectRoleService;

	@BeforeEach
	void setUp() {
		PostReferenceChecker postReferenceChecker = new PostReferenceChecker(sysPostMapper, postReferenceMapper);
		GrantTableSubjectExistenceVerifier subjectExistenceVerifier = new GrantTableSubjectExistenceVerifier(null,
				postReferenceChecker);
		grantTableSubjectRoleService = new GrantTableSubjectRoleServiceImpl(subjectExistenceVerifier, grantTableMapper,
				roleGrantReferenceChecker, grantInvalidationTrigger);
	}

	@Test
	@DisplayName("查询已分配角色：主体存在时返回转换结果")
	void listAssignedRolesReturnsRowsWhenSubjectExists() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost(SUBJECT_ID));
		GrantTableBoundRolePO boundRole = new GrantTableBoundRolePO();
		boundRole.setRoleCode("CUSTOM_A");
		boundRole.setRoleName("业务角色A");
		when(grantTableMapper.selectBoundRolesBySubject(GrantTableSubjectType.POST.name(), SUBJECT_ID,
				RoleTypeEnum.ORG_GRANT_TYPES))
			.thenReturn(List.of(boundRole));

		List<GrantTableAssignedRoleRow> rows = grantTableSubjectRoleService
			.listAssignedRoles(GrantTableSubjectType.POST, SUBJECT_ID);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("CUSTOM_A");
		assertThat(rows.get(0).getRoleName()).isEqualTo("业务角色A");
	}

	@Test
	@DisplayName("查询已分配角色：主体不存在时抛出 POST_NOT_FOUND")
	void listAssignedRolesThrowsWhenSubjectMissing() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(null);

		assertThatThrownBy(() -> grantTableSubjectRoleService.listAssignedRoles(GrantTableSubjectType.POST, SUBJECT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("全量覆盖：写入 grant_table 并提交授权边失效")
	void replaceSubjectRolesWritesGrantAndSubmitsInvalidation() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost(SUBJECT_ID));
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of("BIZ_ADMIN", "CUSTOM_A"));

		grantTableSubjectRoleService.replaceSubjectRoles(GrantTableSubjectType.POST, SUBJECT_ID, form);

		verify(roleGrantReferenceChecker).requireGrantTableOrgRoles(GrantTableSubjectType.POST,
				List.of("BIZ_ADMIN", "CUSTOM_A"));
		verify(grantTableMapper).deleteBySubject(GrantTableSubjectType.POST.name(), SUBJECT_ID);
		verify(grantTableMapper).batchInsertSubjectRoleGrants(eq(GrantTableSubjectType.POST.name()), eq(SUBJECT_ID),
				eq(List.of("BIZ_ADMIN", "CUSTOM_A")), any());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<GrantSubjectKey>> subjectsCaptor = ArgumentCaptor.forClass(List.class);
		verify(grantInvalidationTrigger).submitByGrantSubjects(subjectsCaptor.capture(),
				eq("replace-roles"));
		assertThat(subjectsCaptor.getValue())
			.containsExactly(new GrantSubjectKey(GrantTableSubjectType.POST, SUBJECT_ID));
	}

	@Test
	@DisplayName("全量覆盖：空角色列表仅删除 grant 行仍触发失效")
	void replaceSubjectRolesClearsGrantsWhenRoleCodesEmpty() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost(SUBJECT_ID));
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of());

		grantTableSubjectRoleService.replaceSubjectRoles(GrantTableSubjectType.POST, SUBJECT_ID, form);

		verify(grantTableMapper).deleteBySubject(GrantTableSubjectType.POST.name(), SUBJECT_ID);
		verify(grantTableMapper, never()).batchInsertSubjectRoleGrants(any(), any(), any(), any());
		verify(grantInvalidationTrigger).submitByGrantSubjects(any(), eq("replace-roles"));
	}

	private static SysPostEntity activePost(Long id) {
		SysPostEntity entity = new SysPostEntity();
		entity.setId(id);
		entity.setDeptId(1L);
		entity.setPostCode("DEV");
		entity.setPostName("开发岗");
		entity.setStatus(true);
		return entity;
	}

}
