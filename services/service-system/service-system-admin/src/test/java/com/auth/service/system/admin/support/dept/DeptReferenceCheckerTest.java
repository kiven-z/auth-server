package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.mapper.dept.DeptReferenceCheckerMapper;
import com.auth.service.system.admin.mapper.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * {@link DeptReferenceChecker} 单元测试。
 */
@DisplayName("DeptReferenceChecker 部门引用校验")
@ExtendWith(MockitoExtension.class)
class DeptReferenceCheckerTest {

	private static final Long DEPT_ID = 10L;

	private static final Long PARENT_ID = 8L;

	private static final Long CHILD_DEPT_ID = 20L;

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private DeptReferenceCheckerMapper deptReferenceCheckerMapper;

	@InjectMocks
	private DeptReferenceChecker deptReferenceChecker;

	@Test
	@DisplayName("getExistingActive：部门存在时返回实体")
	void getExistingActiveReturnsEntityWhenPresent() {
		SysDeptEntity entity = activeDept(DEPT_ID);
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(entity);

		assertThat(deptReferenceChecker.getExistingActive(DEPT_ID)).isSameAs(entity);
		verify(sysDeptMapper).selectById(DEPT_ID);
		verifyNoInteractions(deptReferenceCheckerMapper);
	}

	@Test
	@DisplayName("getExistingActive：部门不存在时抛出 DATA_NOT_EXIST")
	void getExistingActiveThrowsWhenMissing() {
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(null);

		assertThatThrownBy(() -> deptReferenceChecker.getExistingActive(DEPT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("normalizeAndRequireActiveParent：顶级不查库")
	void normalizeAndRequireActiveParentSkipsLookupForRoot() {
		assertThat(deptReferenceChecker.normalizeAndRequireActiveParent(null)).isZero();
		assertThat(deptReferenceChecker.normalizeAndRequireActiveParent(0L)).isZero();

		verifyNoInteractions(sysDeptMapper, deptReferenceCheckerMapper);
	}

	@Test
	@DisplayName("normalizeAndRequireActiveParent：父部门存在时返回规范化 ID")
	void normalizeAndRequireActiveParentReturnsWhenParentExists() {
		when(sysDeptMapper.countActiveById(PARENT_ID)).thenReturn(1);

		assertThat(deptReferenceChecker.normalizeAndRequireActiveParent(PARENT_ID)).isEqualTo(PARENT_ID);
		verify(sysDeptMapper).countActiveById(PARENT_ID);
		verifyNoInteractions(deptReferenceCheckerMapper);
	}

	@Test
	@DisplayName("normalizeAndRequireActiveParent：父部门不存在时抛出 TREE_PARENT_UNAVAILABLE")
	void normalizeAndRequireActiveParentThrowsWhenParentMissing() {
		when(sysDeptMapper.countActiveById(9L)).thenReturn(0);

		assertThatThrownBy(() -> deptReferenceChecker.normalizeAndRequireActiveParent(9L))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.TREE_PARENT_UNAVAILABLE);
	}

	@Test
	@DisplayName("requireMoveTargetValid：不能移动到自身")
	void requireMoveTargetValidRejectsSelf() {
		assertThatThrownBy(() -> deptReferenceChecker.requireMoveTargetValid(DEPT_ID, DEPT_ID))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Cannot move tree node under itself, nodeId=" + DEPT_ID);

		verifyNoInteractions(deptReferenceCheckerMapper);
	}

	@Test
	@DisplayName("requireMoveTargetValid：不能移动到后代")
	void requireMoveTargetValidRejectsDescendant() {
		when(deptReferenceCheckerMapper.countDescendantRelation(DEPT_ID, CHILD_DEPT_ID)).thenReturn(1L);

		assertThatThrownBy(() -> deptReferenceChecker.requireMoveTargetValid(DEPT_ID, CHILD_DEPT_ID))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage(
					"Cannot move tree node under its descendant, nodeId=" + DEPT_ID + ", newParentId=" + CHILD_DEPT_ID);

		verify(deptReferenceCheckerMapper).countDescendantRelation(DEPT_ID, CHILD_DEPT_ID);
	}

	@Test
	@DisplayName("requireMoveTargetValid：合法目标不查闭包关系")
	void requireMoveTargetValidAcceptsValidTarget() {
		assertThatCode(() -> deptReferenceChecker.requireMoveTargetValid(DEPT_ID, 0L)).doesNotThrowAnyException();

		verifyNoInteractions(deptReferenceCheckerMapper);
	}

	@Test
	@DisplayName("requireSubtreeUnused：子树无引用时不抛异常")
	void requireSubtreeUnusedPassesWhenUnused() {
		when(deptReferenceCheckerMapper.countUserDeptInSubtree(DEPT_ID)).thenReturn(0L);
		when(deptReferenceCheckerMapper.countGrantDeptSubjectInSubtree(DEPT_ID)).thenReturn(0L);
		when(deptReferenceCheckerMapper.countPostInSubtree(DEPT_ID)).thenReturn(0L);

		assertThatCode(() -> deptReferenceChecker.requireSubtreeUnused(DEPT_ID)).doesNotThrowAnyException();

		verify(deptReferenceCheckerMapper).countUserDeptInSubtree(DEPT_ID);
		verify(deptReferenceCheckerMapper).countGrantDeptSubjectInSubtree(DEPT_ID);
		verify(deptReferenceCheckerMapper).countPostInSubtree(DEPT_ID);
	}

	@Test
	@DisplayName("requireSubtreeUnused：存在用户-部门关联时抛出 DATA_IN_USE")
	void requireSubtreeUnusedThrowsWhenUserDeptReferenced() {
		when(deptReferenceCheckerMapper.countUserDeptInSubtree(DEPT_ID)).thenReturn(1L);

		assertThatThrownBy(() -> deptReferenceChecker.requireSubtreeUnused(DEPT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

		verify(deptReferenceCheckerMapper).countUserDeptInSubtree(DEPT_ID);
		verify(deptReferenceCheckerMapper, never()).countGrantDeptSubjectInSubtree(anyLong());
		verify(deptReferenceCheckerMapper, never()).countPostInSubtree(anyLong());
	}

	@Test
	@DisplayName("requireSubtreeUnused：存在 grant DEPT 引用时抛出 DATA_IN_USE")
	void requireSubtreeUnusedThrowsWhenGrantReferenced() {
		when(deptReferenceCheckerMapper.countUserDeptInSubtree(DEPT_ID)).thenReturn(0L);
		when(deptReferenceCheckerMapper.countGrantDeptSubjectInSubtree(DEPT_ID)).thenReturn(1L);

		assertThatThrownBy(() -> deptReferenceChecker.requireSubtreeUnused(DEPT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

		verify(deptReferenceCheckerMapper).countGrantDeptSubjectInSubtree(DEPT_ID);
		verify(deptReferenceCheckerMapper, never()).countPostInSubtree(anyLong());
	}

	@Test
	@DisplayName("requireSubtreeUnused：存在岗位引用时抛出 DATA_IN_USE")
	void requireSubtreeUnusedThrowsWhenPostReferenced() {
		when(deptReferenceCheckerMapper.countUserDeptInSubtree(DEPT_ID)).thenReturn(0L);
		when(deptReferenceCheckerMapper.countGrantDeptSubjectInSubtree(DEPT_ID)).thenReturn(0L);
		when(deptReferenceCheckerMapper.countPostInSubtree(DEPT_ID)).thenReturn(1L);

		assertThatThrownBy(() -> deptReferenceChecker.requireSubtreeUnused(DEPT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_IN_USE);

		verify(deptReferenceCheckerMapper).countPostInSubtree(DEPT_ID);
	}

	private static SysDeptEntity activeDept(Long id) {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(id);
		entity.setDeptCode("D" + id);
		entity.setDeptName("部门-" + id);
		return entity;
	}

}
