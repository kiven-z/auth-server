package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.post.SysPostMapper;
import com.auth.service.system.admin.mapper.role.GrantTableMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.PostDeptCodePairPO;
import com.auth.service.system.admin.model.po.post.SysPostBoundUserPO;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.po.post.SysPostSearchItemPO;
import com.auth.service.system.admin.model.po.reference.DeptReferencePO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageVO;
import com.auth.service.system.admin.model.vo.post.SysPostSearchItemVO;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.PostAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
	private GrantTableMapper grantTableMapper;

	private SysPostServiceImpl sysPostService;

	private SysPostQueryServiceImpl sysPostQueryService;

	private static SysPostForm minimalForm(Long deptId, String postCode, String postName) {
		return minimalForm(null, deptId, postCode, postName);
	}

	private static SysPostForm minimalForm(Long id, Long deptId, String postCode, String postName) {
		SysPostForm form = new SysPostForm();
		form.setId(id);
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
		PostReferenceChecker postReferenceChecker = new PostReferenceChecker(sysPostMapper);
		sysPostService = spy(new SysPostServiceImpl(postInvalidationTrigger, postReferenceChecker));
		sysPostQueryService = new SysPostQueryServiceImpl(auditUserDisplayService, postReferenceChecker,
				grantTableMapper);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysPostService, sysPostMapper);
		baseMapperField.set(sysPostQueryService, sysPostMapper);
		lenient().doReturn(true).when(sysPostService).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("搜索：关键字原样传入 Mapper")
	void searchPassesKeywordThrough() {
		when(sysPostMapper.search("", true, 10)).thenReturn(List.of());

		sysPostQueryService.search("", true, 10);

		verify(sysPostMapper).search("", true, 10);
	}

	@Test
	@DisplayName("搜索：limit 为 null 时默认使用 20")
	void searchUsesDefaultLimitWhenNull() {
		when(sysPostMapper.search(any(), any(), anyInt())).thenReturn(List.of());

		sysPostQueryService.search("dev", true, null);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysPostMapper).search(eq("dev"), eq(true), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(20);
	}

	@Test
	@DisplayName("搜索：limit 超过 50 时封顶为 50")
	void searchCapsLimitAtFifty() {
		when(sysPostMapper.search(any(), any(), anyInt())).thenReturn(List.of());

		sysPostQueryService.search("研发", null, 200);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysPostMapper).search(eq("研发"), isNull(), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(50);
	}

	@Test
	@DisplayName("搜索：limit 小于 1 时提升为 1")
	void searchRaisesTinyLimitToOne() {
		when(sysPostMapper.search(any(), any(), anyInt())).thenReturn(List.of());

		sysPostQueryService.search("p1", false, 0);

		ArgumentCaptor<Integer> limitCap = ArgumentCaptor.forClass(Integer.class);
		verify(sysPostMapper).search(eq("p1"), eq(false), limitCap.capture());
		assertThat(limitCap.getValue()).isEqualTo(1);
	}

	@Test
	@DisplayName("搜索：正常返回 Mapper 结果")
	void searchReturnsMapperRows() {
		SysPostSearchItemPO row = new SysPostSearchItemPO();
		row.setId(5L);
		row.setPostCode("OPS");
		row.setPostName("运维");
		when(sysPostMapper.search("ops", true, 15)).thenReturn(List.of(row));

		List<SysPostSearchItemVO> result = sysPostQueryService.search("ops", true, 15);

		assertThat(result).hasSize(1).first().satisfies(r -> {
			assertThat(r.getId()).isEqualTo(5L);
			assertThat(r.getPostCode()).isEqualTo("OPS");
		});
	}

	@Test
	@DisplayName("分页查询：委托审计用户名补全")
	void getPageDelegatesEnrichToAuditUserDisplayService() {
		Page<SysPostEntity> pageParams = new Page<>(1, 10);
		SysPostQuery query = new SysPostQuery();
		Page<SysPostPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(new SysPostPageRowPO()));
		when(sysPostMapper.selectListByPage(eq(pageParams), eq(query), anyString())).thenReturn(poPage);

		PageResponse<SysPostPageVO> result = sysPostQueryService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());
	}

	@Test
	@DisplayName("详情：岗位不存在时 POST_NOT_FOUND")
	void getDetailThrowsWhenPostNotFound() {
		when(sysPostMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysPostQueryService.getDetail(9L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);

		verifyNoInteractions(grantTableMapper);
		verify(auditUserDisplayService, never()).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

	@Test
	@DisplayName("详情：组装主表、部门快照、用户与角色并补全审计用户名")
	void getDetailAssemblesBoundDataAndEnrichesAudit() {
		Long postId = 30L;
		SysPostEntity entity = activePost(postId, 2L, "DEV");
		entity.setPostName("研发");
		entity.setOrderNum(1);
		entity.setRemark("备注");

		DeptReferencePO boundDept = new DeptReferencePO();
		boundDept.setId(2L);
		boundDept.setDeptCode("RD");
		boundDept.setDeptName("研发部");
		boundDept.setStatus(true);

		SysPostBoundUserPO boundUser = new SysPostBoundUserPO();
		boundUser.setId(100L);
		boundUser.setUsername("alice");

		RoleReferencePO boundRole = new RoleReferencePO();
		boundRole.setRoleCode("CUSTOM_A");
		boundRole.setRoleName("业务角色A");

		when(sysPostMapper.selectById(postId)).thenReturn(entity);
		when(sysPostMapper.selectBoundDeptByPostId(postId)).thenReturn(boundDept);
		when(sysPostMapper.selectBoundUsersByPostId(postId)).thenReturn(List.of(boundUser));
		when(grantTableMapper.selectBoundRolesBySubject(GrantTableSubjectType.POST.name(), postId))
			.thenReturn(List.of(boundRole));

		SysPostDetailVO detail = sysPostQueryService.getDetail(postId);

		assertThat(detail.getPostCode()).isEqualTo("DEV");
		assertThat(detail.getPostName()).isEqualTo("研发");
		assertThat(detail.getDeptId()).isEqualTo(2L);
		assertThat(detail.getBoundDept()).isNotNull();
		assertThat(detail.getBoundDept().getDeptName()).isEqualTo("研发部");
		assertThat(detail.getBoundUsers()).hasSize(1);
		assertThat(detail.getBoundUsers().get(0).getUsername()).isEqualTo("alice");
		assertThat(detail.getBoundRoles()).hasSize(1);
		assertThat(detail.getBoundRoles().get(0).getRoleCode()).isEqualTo("CUSTOM_A");
		verify(sysPostMapper).selectBoundDeptByPostId(postId);
		verify(sysPostMapper).selectBoundUsersByPostId(postId);
		verify(grantTableMapper).selectBoundRolesBySubject(GrantTableSubjectType.POST.name(), postId);
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

	@Test
	@DisplayName("批量新增岗位：空列表直接返回")
	void createBatchFromImportSkipsWhenEmpty() {
		sysPostService.createBatchFromImport(List.of());

		verify(sysPostMapper, never()).selectAssignableDeptIds(any());
		verify(sysPostService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("新增：部门不可挂载时 POST_DEPT_NOT_FOUND")
	void createThrowsWhenDeptNotAssignable() {
		when(sysPostMapper.selectAssignableDeptIds(List.of(99L))).thenReturn(List.of());
		SysPostForm form = minimalForm(99L, "P1", "开发");

		ThrowingCallable executable = () -> sysPostService.createBatchFromImport(List.of(form));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	@Test
	@DisplayName("新增：父级停用导致子级不可挂载时 POST_DEPT_NOT_FOUND")
	void createThrowsWhenAncestorDeptDisabled() {
		// 子部门自身可启用，但祖先停用后 selectAssignableDeptIds 为空
		when(sysPostMapper.selectAssignableDeptIds(List.of(100L))).thenReturn(List.of());
		SysPostForm form = minimalForm(100L, "P2", "子岗");

		ThrowingCallable executable = () -> sysPostService.createBatchFromImport(List.of(form));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);

		verify(sysPostMapper, atLeastOnce()).selectAssignableDeptIds(List.of(100L));
		verify(sysPostService, never()).saveBatch(anyList(), anyInt());
	}

	@Test
	@DisplayName("新增：部门内编码重复时 POST_CODE_DUPLICATE_IN_DEPT")
	void createThrowsWhenPostCodeDuplicateInDept() {
		when(sysPostMapper.selectAssignableDeptIds(List.of(1L))).thenReturn(List.of(1L));
		PostDeptCodePairPO existing = new PostDeptCodePairPO();
		existing.setDeptId(1L);
		existing.setPostCode("DUP");
		when(sysPostMapper.selectReferenceByDeptPostPairs(anyList())).thenReturn(List.of(existing));
		SysPostForm form = minimalForm(1L, "DUP", "重复岗");

		ThrowingCallable executable = () -> sysPostService.createBatchFromImport(List.of(form));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.POST_CODE_DUPLICATE_IN_DEPT);

		verify(sysPostMapper).selectReferenceByDeptPostPairs(anyList());
		verify(sysPostMapper, never()).selectList(any());
	}

	@Test
	@DisplayName("新增：校验通过时批量落库")
	void createPersistsWhenValid() {
		when(sysPostMapper.selectAssignableDeptIds(List.of(2L))).thenReturn(List.of(2L));
		when(sysPostMapper.selectReferenceByDeptPostPairs(anyList())).thenReturn(List.of());
		SysPostForm form = minimalForm(2L, "NEW_CODE", "新岗位");

		sysPostService.createBatchFromImport(List.of(form));

		verify(sysPostMapper).selectReferenceByDeptPostPairs(anyList());
		verify(sysPostMapper, never()).selectList(any());
		verify(sysPostService).saveBatch(
				argThat((List<SysPostEntity> entities) -> entities.size() == 1
						&& Long.valueOf(2L).equals(entities.get(0).getDeptId())
						&& "NEW_CODE".equals(entities.get(0).getPostCode()) && entities.get(0).getStatus() != null
						&& entities.get(0).getStatus() && Integer.valueOf(0).equals(entities.get(0).getOrderNum())),
				eq(500));
	}

	@Test
	@DisplayName("更新：岗位不存在时 POST_NOT_FOUND")
	void updateThrowsWhenPostNotFound() {
		when(sysPostMapper.selectById(9L)).thenReturn(null);
		SysPostForm form = minimalForm(9L, 1L, "C1", "岗位");

		assertThatThrownBy(() -> sysPostService.update(form)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("更新：仅改名称时不提交授权失效")
	void updateWithoutStatusChangeSkipsInvalidation() {
		Long postId = 10L;
		SysPostEntity existing = activePost(postId, 1L, "DEV");
		when(sysPostMapper.selectById(postId)).thenReturn(existing);
		when(sysPostMapper.selectAssignableDeptIds(List.of(1L))).thenReturn(List.of(1L));
		when(sysPostMapper.selectCount(any())).thenReturn(0L);
		when(sysPostMapper.updateById(any(SysPostEntity.class))).thenReturn(1);

		SysPostForm form = minimalForm(postId, 1L, "DEV", "研发岗");
		form.setStatus(true);

		sysPostService.update(form);

		verify(sysPostMapper).updateById(argThat((SysPostEntity e) -> "研发岗".equals(e.getPostName())));
		verify(postInvalidationTrigger, never()).submitByPostIds(any(), any());
	}

	@Test
	@DisplayName("更新：仅改所属部门时不提交授权失效")
	void updateDeptChangeWithoutStatusSkipsInvalidation() {
		Long postId = 12L;
		SysPostEntity existing = activePost(postId, 1L, "DEV");
		when(sysPostMapper.selectById(postId)).thenReturn(existing);
		when(sysPostMapper.selectAssignableDeptIds(List.of(5L))).thenReturn(List.of(5L));
		when(sysPostMapper.selectCount(any())).thenReturn(0L);
		when(sysPostMapper.updateById(any(SysPostEntity.class))).thenReturn(1);

		SysPostForm form = minimalForm(postId, 5L, "DEV", "原名");
		form.setStatus(true);

		sysPostService.update(form);

		verify(sysPostMapper).selectAssignableDeptIds(List.of(5L));
		verify(sysPostMapper).updateById(argThat((SysPostEntity e) -> Long.valueOf(5L).equals(e.getDeptId())));
		verify(postInvalidationTrigger, never()).submitByPostIds(any(), any());
	}

	@Test
	@DisplayName("更新：状态变更时提交 USER_POST 授权失效")
	void updateWithStatusChangeSubmitsInvalidation() {
		Long postId = 11L;
		SysPostEntity existing = activePost(postId, 2L, "OPS");
		when(sysPostMapper.selectById(postId)).thenReturn(existing);
		when(sysPostMapper.selectAssignableDeptIds(List.of(2L))).thenReturn(List.of(2L));
		when(sysPostMapper.selectCount(any())).thenReturn(0L);
		when(sysPostMapper.updateById(any(SysPostEntity.class))).thenReturn(1);

		SysPostForm form = minimalForm(postId, 2L, "OPS", "运维");
		form.setStatus(false);

		sysPostService.update(form);

		verify(sysPostMapper).updateById(argThat((SysPostEntity e) -> Long.valueOf(2L).equals(e.getDeptId())
				&& e.getStatus() != null && !e.getStatus()));
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
		when(sysPostMapper.countUserPostByPostId(postId)).thenReturn(1L);

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
		when(sysPostMapper.countUserPostByPostId(postId)).thenReturn(0L);
		when(sysPostMapper.countGrantPostSubjectByPostId(postId)).thenReturn(1L);

		assertThatThrownBy(() -> sysPostService.deleteById(postId)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

		verify(sysPostMapper, never()).deleteById(any());
		verifyNoInteractions(postInvalidationTrigger);
	}

	@Test
	@DisplayName("删除：无引用时物理删除并仅提交 USER_POST 授权失效")
	void deleteShouldInvalidateAndRemoveWhenDeletable() {
		Long postId = 22L;
		when(sysPostMapper.selectById(postId)).thenReturn(activePost(postId, 1L, "OK"));
		when(sysPostMapper.countUserPostByPostId(postId)).thenReturn(0L);
		when(sysPostMapper.countGrantPostSubjectByPostId(postId)).thenReturn(0L);
		when(sysPostMapper.deleteById(postId)).thenReturn(1);

		sysPostService.deleteById(postId);

		verify(postInvalidationTrigger).submitByPostIds(List.of(postId), "delete");
		verify(sysPostMapper).deleteById(postId);
	}

}
