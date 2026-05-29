package com.auth.service.system.admin.service.impl;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptCreateForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysDeptCommandServiceImpl} 单元测试。
 */
@DisplayName("SysDeptCommandServiceImpl 部门写操作")
@ExtendWith(MockitoExtension.class)
class SysDeptCommandServiceImplTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private DeptReferenceChecker deptReferenceChecker;

	@Mock
	private DeptClosureMaintainer deptClosureMaintainer;

	@Mock
	private DeptAuthorizationInvalidationTrigger deptInvalidationTrigger;

	private SysDeptCommandServiceImpl commandService;

	@BeforeEach
	void setUp() throws Exception {
		commandService = new SysDeptCommandServiceImpl(deptReferenceChecker, deptClosureMaintainer,
				deptInvalidationTrigger);
		Field baseMapper = commandService.getClass().getSuperclass().getDeclaredField("baseMapper");
		baseMapper.setAccessible(true);
		baseMapper.set(commandService, sysDeptMapper);
	}

	@Test
	@DisplayName("新增部门：保存后调用闭包 insert")
	void create_insertsClosureAfterSave() {
		SysDeptCreateForm form = new SysDeptCreateForm();
		form.setParentId(1L);
		form.setDeptName("研发一组");
		form.setDeptCode("RD_G1");
		form.setStatus(true);

		doAnswer(invocation -> {
			SysDeptEntity entity = invocation.getArgument(0);
			entity.setId(100L);
			return 1;
		}).when(sysDeptMapper).insert(any(SysDeptEntity.class));

		commandService.create(form);

		verify(deptClosureMaintainer).insertNode(100L, 1L);
	}

	@Test
	@DisplayName("移动部门：父未变时不调用闭包 move")
	void move_skipsWhenParentUnchanged() {
		SysDeptEntity existing = activeDept(10L, 1L);
		when(sysDeptMapper.selectById(10L)).thenReturn(existing);

		SysDeptMoveForm form = new SysDeptMoveForm();
		form.setParentId(1L);
		commandService.move(10L, form);

		verify(deptClosureMaintainer, org.mockito.Mockito.never()).moveNode(any(Long.class), any(Long.class));
	}

	@Test
	@DisplayName("删除部门：存在子部门时抛出 DEPT_HAS_CHILDREN")
	void delete_throwsWhenHasChildren() {
		SysDeptEntity existing = activeDept(5L, 1L);
		when(sysDeptMapper.selectById(5L)).thenReturn(existing);
		org.mockito.Mockito.doThrow(new SystemBusinessException(SystemAdminResultCode.DEPT_HAS_CHILDREN, 5L))
			.when(deptReferenceChecker)
			.requireNoActiveChildren(5L);

		assertThatThrownBy(() -> commandService.deleteById(5L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.DEPT_HAS_CHILDREN);
	}

	@Test
	@DisplayName("移动部门：父变更后调用闭包 move 并提交失效")
	void move_callsClosureAndInvalidation() {
		SysDeptEntity existing = activeDept(10L, 1L);
		when(sysDeptMapper.selectById(10L)).thenReturn(existing);

		SysDeptMoveForm form = new SysDeptMoveForm();
		form.setParentId(2L);
		commandService.move(10L, form);

		verify(deptClosureMaintainer).moveNode(10L, 2L);
		verify(deptInvalidationTrigger).submitByDeptIds(eq(java.util.List.of(10L)),
				org.mockito.ArgumentMatchers.contains("move"));
	}

	private static SysDeptEntity activeDept(long id, long parentId) {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(id);
		entity.setParentId(parentId);
		entity.setDeptCode("D" + id);
		entity.setDeptName("Dept" + id);
		entity.setStatus(true);
		entity.setIsDeleted(0);
		return entity;
	}

}
