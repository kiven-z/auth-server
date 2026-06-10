package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.admin.mapper.GrantTableMapper;
import com.auth.service.system.admin.mapper.PostReferenceMapper;
import com.auth.service.system.admin.mapper.SysPostDetailMapper;
import com.auth.service.system.admin.mapper.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.enums.RoleTypeEnum;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundRolePO;
import com.auth.service.system.admin.model.po.post.SysPostBoundDeptPO;
import com.auth.service.system.admin.model.po.post.SysPostBoundUserPO;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageResponse;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.PostAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
 * {@link SysPostServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysPostServiceImpl 岗位管理")
@ExtendWith(MockitoExtension.class)
class SysPostServiceImplTest {

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private PostAuthorizationInvalidationTrigger postInvalidationTrigger;

	@Mock
	private PostReferenceMapper postReferenceMapper;

	@Mock
	private SysPostDetailMapper sysPostDetailMapper;

	@Mock
	private GrantTableMapper grantTableMapper;

	private SysPostServiceImpl sysPostService;

	private static SysPostForm minimalForm(Long deptId, String postCode, String postName) {
		SysPostForm form = new SysPostForm();
		form.setDeptId(deptId);
		form.setPostCode(postCode);
		form.setPostName(postName);
		form.setStatus(true);
		form.setOrderNum(0);
		return form;
	}

	private static SysPostEntity activePost(Long id, Long deptId, String postCode) {
		SysPostEntity entity = new SysPostEntity();
		entity.setId(id);
		entity.setDeptId(deptId);
		entity.setPostCode(postCode);
		entity.setPostName("原名");
		entity.setStatus(true);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		PostReferenceChecker postReferenceChecker = new PostReferenceChecker(sysPostMapper, postReferenceMapper);
		sysPostService = new SysPostServiceImpl(auditUserDisplayService, postInvalidationTrigger, postReferenceChecker,
				sysPostDetailMapper, grantTableMapper);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysPostService, sysPostMapper);
	}

	@Test
	@DisplayName("分页查询：委托审计用户名补全")
	void getPageDelegatesEnrichToAuditUserDisplayService() {
		Page<SysPostEntity> pageParams = new Page<>(1, 10);
		SysPostQuery query = new SysPostQuery();
		Page<SysPostPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(new SysPostPageRowPO()));
		when(sysPostMapper.selectListByPage(eq(pageParams), eq(query), anyString())).thenReturn(poPage);

		PageResponse<SysPostPageResponse> result = sysPostService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class));
	}

	@Test
	@DisplayName("详情：岗位不存在时 POST_NOT_FOUND")
	void getDetailThrowsWhenPostNotFound() {
		when(sysPostMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPostService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(sysPostDetailMapper);
		verify(auditUserDisplayService, never()).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("详情：组装主表、部门快照、用户与角色并补全审计用户名")
	void getDetailAssemblesBoundDataAndEnrichesAudit() {
		Long postId = 30L;
		SysPostEntity entity = activePost(postId, 2L, "DEV");
		entity.setPostName("研发");
		entity.setOrderNum(1);
		entity.setRemark("备注");

		SysPostBoundDeptPO boundDept = new SysPostBoundDeptPO();
		boundDept.setDeptId(2L);
		boundDept.setDeptCode("RD");
		boundDept.setDeptName("研发部");
		boundDept.setStatus(true);
		boundDept.setParentId(0L);

		SysPostBoundUserPO boundUser = new SysPostBoundUserPO();
		boundUser.setUserId(100L);
		boundUser.setUsername("alice");

		GrantTableBoundRolePO boundRole = new GrantTableBoundRolePO();
		boundRole.setRoleCode("CUSTOM_A");
		boundRole.setRoleName("业务角色A");

		when(sysPostMapper.selectById(postId)).thenReturn(entity);
		when(sysPostDetailMapper.selectBoundDeptByPostId(postId)).thenReturn(boundDept);
		when(sysPostDetailMapper.selectBoundUsersByPostId(postId)).thenReturn(List.of(boundUser));
		when(grantTableMapper.selectBoundRolesBySubject(GrantTableSubjectType.POST.name(), postId,
				RoleTypeEnum.SUBJECT_GRANT_TYPES))
			.thenReturn(List.of(boundRole));

		SysPostDetailVO detail = sysPostService.getDetail(postId);

		assertThat(detail.getPostCode()).isEqualTo("DEV");
		assertThat(detail.getPostName()).isEqualTo("研发");
		assertThat(detail.getDeptId()).isEqualTo(2L);
		assertThat(detail.getBoundDept()).isNotNull();
		assertThat(detail.getBoundDept().getDeptName()).isEqualTo("研发部");
		assertThat(detail.getBoundUsers()).hasSize(1);
		assertThat(detail.getBoundUsers().get(0).getUsername()).isEqualTo("alice");
		assertThat(detail.getBoundRoles()).hasSize(1);
		assertThat(detail.getBoundRoles().get(0).getRoleCode()).isEqualTo("CUSTOM_A");
		verify(sysPostDetailMapper).selectBoundDeptByPostId(postId);
		verify(sysPostDetailMapper).selectBoundUsersByPostId(postId);
		verify(grantTableMapper).selectBoundRolesBySubject(GrantTableSubjectType.POST.name(), postId,
				RoleTypeEnum.SUBJECT_GRANT_TYPES);
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("新增：部门不可挂载时 POST_DEPT_NOT_FOUND")
	void createThrowsWhenDeptNotAssignable() {
		when(sysPostMapper.countAssignableDept(99L)).thenReturn(0);
		SysPostForm form = minimalForm(99L, "P1", "开发");

		assertThatThrownBy(() -> sysPostService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	@Test
	@DisplayName("新增：父级停用导致子级不可挂载时 POST_DEPT_NOT_FOUND")
	void createThrowsWhenAncestorDeptDisabled() {
		// 子部门自身可启用，但祖先停用后 countAssignableDept 为 0
		when(sysPostMapper.countAssignableDept(100L)).thenReturn(0);
		SysPostForm form = minimalForm(100L, "P2", "子岗");

		assertThatThrownBy(() -> sysPostService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);

		verify(sysPostMapper).countAssignableDept(100L);
		verify(sysPostMapper, never()).insert(any(SysPostEntity.class));
	}

	@Test
	@DisplayName("新增：部门内编码重复时 POST_CODE_DUPLICATE_IN_DEPT")
	void createThrowsWhenPostCodeDuplicateInDept() {
		when(sysPostMapper.countAssignableDept(1L)).thenReturn(1);
		when(sysPostMapper.selectCount(any())).thenReturn(1L);
		SysPostForm form = minimalForm(1L, "DUP", "重复岗");

		assertThatThrownBy(() -> sysPostService.create(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.POST_CODE_DUPLICATE_IN_DEPT);
	}

	@Test
	@DisplayName("新增：校验通过时落库")
	void createPersistsWhenValid() {
		when(sysPostMapper.countAssignableDept(2L)).thenReturn(1);
		when(sysPostMapper.selectCount(any())).thenReturn(0L);
		when(sysPostMapper.insert(any(SysPostEntity.class))).thenReturn(1);
		SysPostForm form = minimalForm(2L, "NEW_CODE", "新岗位");

		sysPostService.create(form);

		verify(sysPostMapper).insert(argThat(
				(SysPostEntity e) -> Long.valueOf(2L).equals(e.getDeptId()) && "NEW_CODE".equals(e.getPostCode())
						&& Boolean.TRUE.equals(e.getStatus()) && Integer.valueOf(0).equals(e.getOrderNum())));
	}

	@Test
	@DisplayName("更新：岗位不存在时 POST_NOT_FOUND")
	void updateThrowsWhenPostNotFound() {
		when(sysPostMapper.selectById(9L)).thenReturn(null);
		SysPostForm form = minimalForm(1L, "C1", "岗位");

		assertThatThrownBy(() -> sysPostService.update(9L, form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("更新：仅改名称时不提交授权失效")
	void updateWithoutStatusChangeSkipsInvalidation() {
		Long postId = 10L;
		SysPostEntity existing = activePost(postId, 1L, "DEV");
		when(sysPostMapper.selectById(postId)).thenReturn(existing);
		when(sysPostMapper.countAssignableDept(1L)).thenReturn(1);
		when(sysPostMapper.selectCount(any())).thenReturn(0L);
		when(sysPostMapper.updateById(any(SysPostEntity.class))).thenReturn(1);

		SysPostForm form = minimalForm(1L, "DEV", "研发岗");
		form.setStatus(true);

		sysPostService.update(postId, form);

		verify(sysPostMapper).updateById(argThat((SysPostEntity e) -> "研发岗".equals(e.getPostName())));
		verify(postInvalidationTrigger, never()).submitByPostIds(any(), any());
	}

	@Test
	@DisplayName("更新：仅改所属部门时不提交授权失效")
	void updateDeptChangeWithoutStatusSkipsInvalidation() {
		Long postId = 12L;
		SysPostEntity existing = activePost(postId, 1L, "DEV");
		when(sysPostMapper.selectById(postId)).thenReturn(existing);
		when(sysPostMapper.countAssignableDept(5L)).thenReturn(1);
		when(sysPostMapper.selectCount(any())).thenReturn(0L);
		when(sysPostMapper.updateById(any(SysPostEntity.class))).thenReturn(1);

		SysPostForm form = minimalForm(5L, "DEV", "原名");
		form.setStatus(true);

		sysPostService.update(postId, form);

		verify(sysPostMapper).countAssignableDept(5L);
		verify(sysPostMapper).updateById(argThat((SysPostEntity e) -> Long.valueOf(5L).equals(e.getDeptId())));
		verify(postInvalidationTrigger, never()).submitByPostIds(any(), any());
	}

	@Test
	@DisplayName("更新：状态变更时提交 USER_POST 授权失效")
	void updateWithStatusChangeSubmitsInvalidation() {
		Long postId = 11L;
		SysPostEntity existing = activePost(postId, 2L, "OPS");
		when(sysPostMapper.selectById(postId)).thenReturn(existing);
		when(sysPostMapper.countAssignableDept(2L)).thenReturn(1);
		when(sysPostMapper.selectCount(any())).thenReturn(0L);
		when(sysPostMapper.updateById(any(SysPostEntity.class))).thenReturn(1);

		SysPostForm form = minimalForm(2L, "OPS", "运维");
		form.setStatus(false);

		sysPostService.update(postId, form);

		verify(sysPostMapper).updateById(argThat(
				(SysPostEntity e) -> Long.valueOf(2L).equals(e.getDeptId()) && Boolean.FALSE.equals(e.getStatus())));
		verify(postInvalidationTrigger).submitByPostIds(List.of(postId), "update");
	}

	@Test
	@DisplayName("删除：岗位不存在时 POST_NOT_FOUND")
	void deleteThrowsWhenPostNotFound() {
		when(sysPostMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPostService.deleteById(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(postInvalidationTrigger);
	}

	@Test
	@DisplayName("删除：user_post 仍有关联时 POST_IN_USE")
	void deleteThrowsWhenUserPostInUse() {
		Long postId = 20L;
		when(sysPostMapper.selectById(postId)).thenReturn(activePost(postId, 1L, "U1"));
		when(postReferenceMapper.countUserPostByPostId(postId)).thenReturn(1L);

		assertThatThrownBy(() -> sysPostService.deleteById(postId)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

		verify(sysPostMapper, never()).deleteById(any());
		verifyNoInteractions(postInvalidationTrigger);
	}

	@Test
	@DisplayName("删除：grant_table POST 主体仍引用时 POST_IN_USE")
	void deleteThrowsWhenGrantPostSubjectInUse() {
		Long postId = 21L;
		when(sysPostMapper.selectById(postId)).thenReturn(activePost(postId, 1L, "G1"));
		when(postReferenceMapper.countUserPostByPostId(postId)).thenReturn(0L);
		when(postReferenceMapper.countGrantPostSubjectByPostId(postId)).thenReturn(1L);

		assertThatThrownBy(() -> sysPostService.deleteById(postId)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

		verify(sysPostMapper, never()).deleteById(any());
		verifyNoInteractions(postInvalidationTrigger);
	}

	@Test
	@DisplayName("删除：无引用时逻辑删除并仅提交 USER_POST 授权失效")
	void deleteShouldInvalidateAndRemoveWhenDeletable() {
		Long postId = 22L;
		when(sysPostMapper.selectById(postId)).thenReturn(activePost(postId, 1L, "OK"));
		when(postReferenceMapper.countUserPostByPostId(postId)).thenReturn(0L);
		when(postReferenceMapper.countGrantPostSubjectByPostId(postId)).thenReturn(0L);
		when(sysPostMapper.deleteById(postId)).thenReturn(1);

		sysPostService.deleteById(postId);

		verify(postInvalidationTrigger).submitByPostIds(List.of(postId), "delete");
		verify(sysPostMapper).deleteById(postId);
	}

}
