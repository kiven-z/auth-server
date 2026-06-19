package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import com.auth.service.system.admin.mapper.post.PostReferenceCheckerMapper;
import com.auth.service.system.admin.mapper.post.SysPostMapper;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.granttable.GrantTableAssignRoleForm;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundRolePO;
import com.auth.service.system.admin.model.vo.granttable.GrantTableAssignedRoleRow;
import com.auth.service.system.admin.support.granttable.GrantTableSubjectExistenceVerifier;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.role.RoleGrantReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.GrantAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link GrantTableServiceImpl} 单元测试
 */
@DisplayName("GrantTableServiceImpl grant_table 读写与授权编排")
@ExtendWith(MockitoExtension.class)
class GrantTableServiceImplTest {

	private static final Long SUBJECT_ID = 10L;

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private PostReferenceCheckerMapper postReferenceCheckerMapper;

	@Mock
	private GrantTableMapper grantTableMapper;

	@Mock
	private RoleGrantReferenceChecker roleGrantReferenceChecker;

	@Mock
	private GrantAuthorizationInvalidationTrigger grantInvalidationTrigger;

	private GrantTableServiceImpl grantTableService;

	private static SysPostEntity activePost() {
		SysPostEntity entity = new SysPostEntity();
		entity.setId(GrantTableServiceImplTest.SUBJECT_ID);
		entity.setDeptId(1L);
		entity.setPostCode("DEV");
		entity.setPostName("开发岗");
		entity.setStatus(true);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		PostReferenceChecker postReferenceChecker = new PostReferenceChecker(sysPostMapper, postReferenceCheckerMapper);
		GrantTableSubjectExistenceVerifier subjectExistenceVerifier = new GrantTableSubjectExistenceVerifier(null,
				postReferenceChecker);
		grantTableService = spy(new GrantTableServiceImpl(subjectExistenceVerifier, roleGrantReferenceChecker,
				grantInvalidationTrigger));
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(grantTableService, grantTableMapper);
		lenient().doReturn(true).when(grantTableService).saveBatch(any(Collection.class));
	}

	@Test
	@DisplayName("查询已分配角色：主体存在时返回转换结果")
	void listAssignedRolesReturnsRowsWhenSubjectExists() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost());
		GrantTableBoundRolePO boundRole = new GrantTableBoundRolePO();
		boundRole.setRoleCode("CUSTOM_A");
		boundRole.setRoleName("业务角色A");
		when(grantTableMapper.selectBoundRolesBySubject(GrantTableSubjectType.POST.name(), SUBJECT_ID))
			.thenReturn(List.of(boundRole));

		List<GrantTableAssignedRoleRow> rows = grantTableService.listAssignedRoles(GrantTableSubjectType.POST,
				SUBJECT_ID);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("CUSTOM_A");
		assertThat(rows.get(0).getRoleName()).isEqualTo("业务角色A");
	}

	@Test
	@DisplayName("查询已分配角色：包含非主体授权类型角色时仍返回")
	void listAssignedRolesReturnsLinkedRoleRegardlessOfType() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost());
		GrantTableBoundRolePO menuRole = new GrantTableBoundRolePO();
		menuRole.setRoleCode("MENU_ADMIN");
		menuRole.setRoleName("菜单管理员");
		when(grantTableMapper.selectBoundRolesBySubject(GrantTableSubjectType.POST.name(), SUBJECT_ID))
			.thenReturn(List.of(menuRole));

		List<GrantTableAssignedRoleRow> rows = grantTableService.listAssignedRoles(GrantTableSubjectType.POST,
				SUBJECT_ID);

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getRoleCode()).isEqualTo("MENU_ADMIN");
		assertThat(rows.get(0).getRoleName()).isEqualTo("菜单管理员");
	}

	@Test
	void listAssignedRolesThrowsWhenSubjectMissing() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(null);

		assertThatThrownBy(() -> grantTableService.listAssignedRoles(GrantTableSubjectType.POST, SUBJECT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("组织类全量覆盖：校验、写入 grant_table 并提交授权边失效")
	void replaceOrgSubjectRolesWritesGrantAndSubmitsInvalidation() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost());
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of("BIZ_ADMIN", "CUSTOM_A"));

		grantTableService.replaceOrgSubjectRoles(GrantTableSubjectType.POST, SUBJECT_ID, form);

		verify(roleGrantReferenceChecker).requireGrantTableOrgRoles(GrantTableSubjectType.POST,
				List.of("BIZ_ADMIN", "CUSTOM_A"));
		verify(grantTableMapper).deleteBySubject(GrantTableSubjectType.POST.name(), SUBJECT_ID);
		verify(grantTableService).saveBatch(any(Collection.class));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<GrantSubjectKey>> subjectsCaptor = ArgumentCaptor.forClass(List.class);
		verify(grantInvalidationTrigger).submitByGrantSubjects(subjectsCaptor.capture(), eq("replace-roles"));
		assertThat(subjectsCaptor.getValue())
			.containsExactly(new GrantSubjectKey(GrantTableSubjectType.POST, SUBJECT_ID));
	}

	@Test
	@DisplayName("组织类全量覆盖：空角色列表仅删除 grant 行仍触发失效")
	void replaceOrgSubjectRolesClearsGrantsWhenRoleCodesEmpty() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(activePost());
		GrantTableAssignRoleForm form = new GrantTableAssignRoleForm();
		form.setRoleCodes(List.of());

		grantTableService.replaceOrgSubjectRoles(GrantTableSubjectType.POST, SUBJECT_ID, form);

		verify(grantTableMapper).deleteBySubject(GrantTableSubjectType.POST.name(), SUBJECT_ID);
		verify(grantTableService, never()).saveBatch(any(Collection.class));
		verify(grantInvalidationTrigger).submitByGrantSubjects(any(), eq("replace-roles"));
	}

	@Test
	@DisplayName("全量覆盖：先删后批量 insert 授权边")
	void replaceSubjectRoleGrantsDeletesThenInsertsEntities() {
		grantTableService.replaceSubjectRoleGrants("USER", SUBJECT_ID, List.of("BIZ_ADMIN", "CUSTOM_A"));

		verify(grantTableMapper).deleteBySubject("USER", SUBJECT_ID);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<GrantTableEntity>> entitiesCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(grantTableService).saveBatch(entitiesCaptor.capture());
		assertThat(entitiesCaptor.getValue()).hasSize(2);
		assertThat(entitiesCaptor.getValue())
			.allSatisfy(entity -> assertThat(entity.getSubjectType()).isEqualTo("USER"))
			.allSatisfy(entity -> assertThat(entity.getSubjectId()).isEqualTo(SUBJECT_ID))
			.allSatisfy(entity -> assertThat(entity.getRoleCode()).isNotBlank());
		assertThat(entitiesCaptor.getValue().stream().map(GrantTableEntity::getRoleCode).toList())
			.containsExactlyInAnyOrder("BIZ_ADMIN", "CUSTOM_A");
	}

	@Test
	@DisplayName("全量覆盖：空角色列表仅删除不 insert")
	void replaceSubjectRoleGrantsDeletesOnlyWhenRoleCodesEmpty() {
		grantTableService.replaceSubjectRoleGrants("DEPT", SUBJECT_ID, List.of());

		verify(grantTableMapper).deleteBySubject("DEPT", SUBJECT_ID);
		verify(grantTableService, never()).saveBatch(any(Collection.class));
	}

}
