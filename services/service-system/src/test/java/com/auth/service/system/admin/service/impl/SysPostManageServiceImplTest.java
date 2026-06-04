package com.auth.service.system.admin.service.impl;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.PostReferenceMapper;
import com.auth.service.system.admin.mapper.SysPostDetailMapper;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostAssignRoleForm;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundRolePO;
import com.auth.service.system.admin.model.vo.post.SysPostAssignedRoleRow;
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
 * {@link SysPostManageServiceImpl} 单元测试
 */
@DisplayName("SysPostManageServiceImpl 岗位角色授权")
@ExtendWith(MockitoExtension.class)
class SysPostManageServiceImplTest {

	private static final Long POST_ID = 10L;

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private PostReferenceMapper postReferenceMapper;

	@Mock
	private SysPostDetailMapper sysPostDetailMapper;

	@Mock
	private GrantTableMapper grantTableMapper;

	@Mock
	private RoleGrantReferenceChecker roleGrantReferenceChecker;

	@Mock
	private GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	private SysPostManageServiceImpl sysPostManageService;

	@BeforeEach
	void setUp() {
		PostReferenceChecker postReferenceChecker = new PostReferenceChecker(sysPostMapper, postReferenceMapper);
		sysPostManageService = new SysPostManageServiceImpl(postReferenceChecker, sysPostDetailMapper, grantTableMapper,
				roleGrantReferenceChecker, grantInvalidationTrigger);
	}

	@Test
	@DisplayName("查询已分配角色：岗位存在时返回转换结果")
	void listAssignedRolesReturnsRowsWhenPostExists() {
		when(sysPostMapper.selectById(POST_ID)).thenReturn(activePost(POST_ID));
		SysDeptBoundRolePO boundRole = new SysDeptBoundRolePO();
		boundRole.setRoleCode("CUSTOM_A");
		boundRole.setRoleName("业务角色A");
		when(sysPostDetailMapper.selectBoundRolesByPostId(POST_ID)).thenReturn(List.of(boundRole));

		List<SysPostAssignedRoleRow> rows = sysPostManageService.listAssignedRoles(POST_ID);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("CUSTOM_A");
		assertThat(rows.get(0).getRoleName()).isEqualTo("业务角色A");
	}

	@Test
	@DisplayName("查询已分配角色：岗位不存在时抛出 POST_NOT_FOUND")
	void listAssignedRolesThrowsWhenPostMissing() {
		when(sysPostMapper.selectById(POST_ID)).thenReturn(null);

		assertThatThrownBy(() -> sysPostManageService.listAssignedRoles(POST_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("全量覆盖：写入 grant_table 并提交授权边失效")
	void replacePostRolesWritesGrantAndSubmitsInvalidation() {
		when(sysPostMapper.selectById(POST_ID)).thenReturn(activePost(POST_ID));
		SysPostAssignRoleForm form = new SysPostAssignRoleForm();
		form.setRoleCodes(List.of("BIZ_ADMIN", "CUSTOM_A"));

		sysPostManageService.replacePostRoles(POST_ID, form);

		verify(roleGrantReferenceChecker).requirePostGrantEligibleRoles(List.of("BIZ_ADMIN", "CUSTOM_A"));
		verify(grantTableMapper).deleteBySubject(GrantTableSubjectType.POST.name(), POST_ID);
		verify(grantTableMapper).batchInsertSubjectRoleGrants(eq(GrantTableSubjectType.POST.name()), eq(POST_ID),
				eq(List.of("BIZ_ADMIN", "CUSTOM_A")), any());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<GrantSubjectKey>> subjectsCaptor = ArgumentCaptor.forClass(List.class);
		verify(grantInvalidationTrigger).submitByGrantSubjects(subjectsCaptor.capture(),
				eq("replace-post-roles:" + POST_ID));
		assertThat(subjectsCaptor.getValue()).containsExactly(new GrantSubjectKey(GrantTableSubjectType.POST, POST_ID));
	}

	@Test
	@DisplayName("全量覆盖：空角色列表仅删除 grant 行仍触发失效")
	void replacePostRolesClearsGrantsWhenRoleCodesEmpty() {
		when(sysPostMapper.selectById(POST_ID)).thenReturn(activePost(POST_ID));
		SysPostAssignRoleForm form = new SysPostAssignRoleForm();
		form.setRoleCodes(List.of());

		sysPostManageService.replacePostRoles(POST_ID, form);

		verify(grantTableMapper).deleteBySubject(GrantTableSubjectType.POST.name(), POST_ID);
		verify(grantTableMapper, never()).batchInsertSubjectRoleGrants(any(), any(), any(), any());
		verify(grantInvalidationTrigger).submitByGrantSubjects(any(), eq("replace-post-roles:" + POST_ID));
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
