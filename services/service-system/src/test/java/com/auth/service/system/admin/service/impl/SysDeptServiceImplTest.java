package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysDeptDetailMapper;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptCreateForm;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundPostPO;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundRolePO;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundUserPO;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysDeptServiceImpl} 单元测试。
 */
@DisplayName("SysDeptServiceImpl 部门管理")
@ExtendWith(MockitoExtension.class)
class SysDeptServiceImplTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private DeptReferenceChecker deptReferenceChecker;

	@Mock
	private DeptClosureMaintainer deptClosureMaintainer;

	@Mock
	private DeptAuthorizationInvalidationTrigger deptInvalidationTrigger;

	@Mock
	private SysDeptDetailMapper sysDeptDetailMapper;

	private SysDeptServiceImpl sysDeptService;

	private static SysDeptEntity activeDept(Long id) {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(id);
		entity.setParentId(0L);
		entity.setDeptName("研发部");
		entity.setDeptCode("RD");
		entity.setStatus(true);
		entity.setOrderNum(1);
		entity.setIsDeleted(0);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysDeptService = spy(new SysDeptServiceImpl(auditUserDisplayService, deptReferenceChecker,
				deptClosureMaintainer, deptInvalidationTrigger, sysDeptDetailMapper));
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysDeptService, sysDeptMapper);
	}

	@Test
	@DisplayName("新增部门：保存后维护闭包表")
	void createPersistsEntityAndMaintainsClosure() {
		SysDeptCreateForm form = new SysDeptCreateForm();
		form.setParentId(1L);
		form.setDeptName("测试部");
		form.setDeptCode("D_TEST");
		form.setStatus(true);
		form.setOrderNum(1);

		when(deptReferenceChecker.normalizeAndRequireActiveParent(1L)).thenReturn(1L);
		when(sysDeptMapper.selectCount(any())).thenReturn(0L);
		doAnswer(invocation -> {
			SysDeptEntity entity = invocation.getArgument(0);
			entity.setId(100L);
			return true;
		}).when(sysDeptService).save(any(SysDeptEntity.class));

		sysDeptService.create(form);

		verify(deptClosureMaintainer).insertNode(100L, 1L);
	}

	@Test
	@DisplayName("部门详情：不存在或已删除时抛出 DEPT_NOT_FOUND")
	void getDetailThrowsWhenNotFound() {
		when(sysDeptMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysDeptService.getDetail(99L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.DEPT_NOT_FOUND);
	}

	@Test
	@DisplayName("部门详情：填充基础字段与三类关联列表")
	void getDetailReturnsVoWithBoundLists() {
		Long deptId = 10L;
		when(sysDeptMapper.selectById(deptId)).thenReturn(activeDept(deptId));

		SysDeptBoundUserPO boundUser = new SysDeptBoundUserPO();
		boundUser.setUserId(100L);
		boundUser.setUsername("alice");
		boundUser.setNickname("Alice");
		boundUser.setIsPrimary(true);
		boundUser.setRelationStatus(1);
		when(sysDeptDetailMapper.selectBoundUsersByDeptId(deptId)).thenReturn(List.of(boundUser));

		SysDeptBoundPostPO boundPost = new SysDeptBoundPostPO();
		boundPost.setId(200L);
		boundPost.setPostCode("DEV");
		boundPost.setPostName("开发岗");
		boundPost.setStatus(true);
		when(sysDeptDetailMapper.selectBoundPostsByDeptId(deptId)).thenReturn(List.of(boundPost));

		SysDeptBoundRolePO boundRole = new SysDeptBoundRolePO();
		boundRole.setRoleCode("STAFF");
		boundRole.setRoleName("普通员工");
		boundRole.setStatus(true);
		boundRole.setRoleType(2);
		when(sysDeptDetailMapper.selectBoundRolesByDeptId(deptId)).thenReturn(List.of(boundRole));

		SysDeptDetailVO detail = sysDeptService.getDetail(deptId);

		assertThat(detail.getDeptCode()).isEqualTo("RD");
		assertThat(detail.getBoundUsers()).hasSize(1);
		assertThat(detail.getBoundUsers().get(0).getUsername()).isEqualTo("alice");
		assertThat(detail.getBoundPosts()).hasSize(1);
		assertThat(detail.getBoundPosts().get(0).getPostCode()).isEqualTo("DEV");
		assertThat(detail.getBoundRoles()).hasSize(1);
		assertThat(detail.getBoundRoles().get(0).getRoleCode()).isEqualTo("STAFF");

		verify(sysDeptDetailMapper).selectBoundUsersByDeptId(deptId);
		verify(sysDeptDetailMapper).selectBoundPostsByDeptId(deptId);
		verify(sysDeptDetailMapper).selectBoundRolesByDeptId(deptId);
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("删除部门：先物理清理闭包再逻辑删主表并触发授权失效")
	void deleteByIdRemovesClosureThenEntity() {
		Long deptId = 20L;
		when(sysDeptMapper.selectById(deptId)).thenReturn(activeDept(deptId));
		doReturn(true).when(sysDeptService).removeById(deptId);

		sysDeptService.deleteById(deptId);

		verify(deptReferenceChecker).requireNoActiveChildren(deptId);
		verify(deptReferenceChecker).requireSubtreeUnused(deptId);
		var inOrder = inOrder(deptClosureMaintainer, sysDeptService);
		inOrder.verify(deptClosureMaintainer).removeClosurePaths(deptId);
		inOrder.verify(sysDeptService).removeById(deptId);
		verify(deptInvalidationTrigger).submitByDeptIds(eq(List.of(deptId)), eq("delete:" + deptId));
	}

}
