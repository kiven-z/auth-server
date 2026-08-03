package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link DeptReferenceChecker} 单元测试。
 */
@DisplayName("DeptReferenceChecker 部门引用校验")
@ExtendWith(MockitoExtension.class)
class DeptReferenceCheckerTest {

	private static final Long DEPT_ID = 10L;

	private static final Long PARENT_ID = 8L;

	@Mock
	private SysDeptMapper sysDeptMapper;

	@InjectMocks
	private DeptReferenceChecker deptReferenceChecker;

	private static SysDeptEntity activeDept() {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(DeptReferenceCheckerTest.DEPT_ID);
		entity.setDeptCode("D" + DeptReferenceCheckerTest.DEPT_ID);
		entity.setDeptName("部门-" + DeptReferenceCheckerTest.DEPT_ID);
		return entity;
	}

	@Test
	@DisplayName("getExistingActive：部门存在时返回实体")
	void getExistingActiveReturnsEntityWhenPresent() {
		SysDeptEntity entity = activeDept();
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(entity);

		assertThat(deptReferenceChecker.getExistingActive(DEPT_ID)).isSameAs(entity);
		verify(sysDeptMapper).selectById(DEPT_ID);
	}

	@Test
	@DisplayName("getExistingActive：部门不存在时抛出 DATA_NOT_EXIST")
	void getExistingActiveThrowsWhenMissing() {
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(null);

		assertThatThrownBy(() -> deptReferenceChecker.getExistingActive(DEPT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.satisfies(ex -> {
				SystemBusinessException biz = (SystemBusinessException) ex;
				assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
				assertThat(biz.getMessageArgs()).isEmpty();
			});
	}

	@Test
	@DisplayName("normalizeAndRequireActiveParent：顶级不查库")
	void normalizeAndRequireActiveParentSkipsLookupForRoot() {
		assertThat(deptReferenceChecker.normalizeAndRequireActiveParent(null)).isZero();
		assertThat(deptReferenceChecker.normalizeAndRequireActiveParent(0L)).isZero();

		verifyNoInteractions(sysDeptMapper);
	}

	@Test
	@DisplayName("normalizeAndRequireActiveParent：父部门存在时返回规范化 ID")
	void normalizeAndRequireActiveParentReturnsWhenParentExists() {
		when(sysDeptMapper.countActiveById(PARENT_ID)).thenReturn(1);

		assertThat(deptReferenceChecker.normalizeAndRequireActiveParent(PARENT_ID)).isEqualTo(PARENT_ID);
		verify(sysDeptMapper).countActiveById(PARENT_ID);
	}

	@Test
	@DisplayName("normalizeAndRequireActiveParent：父部门不存在时抛出 TREE_PARENT_UNAVAILABLE")
	void normalizeAndRequireActiveParentThrowsWhenParentMissing() {
		when(sysDeptMapper.countActiveById(9L)).thenReturn(0);

		assertThatThrownBy(() -> deptReferenceChecker.normalizeAndRequireActiveParent(9L))
			.isInstanceOf(SystemBusinessException.class)
			.satisfies(ex -> {
				SystemBusinessException biz = (SystemBusinessException) ex;
				assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.TREE_PARENT_UNAVAILABLE);
				assertThat(biz.getMessageArgs()).isEmpty();
			});
	}

	@Test
	@DisplayName("requireAbsentDeptCodes：空列表跳过校验")
	void requireAbsentDeptCodesSkipsWhenEmpty() {
		assertThatCode(() -> deptReferenceChecker.requireAbsentDeptCodes(List.of())).doesNotThrowAnyException();

		verifyNoInteractions(sysDeptMapper);
	}

	@Test
	@DisplayName("requireAbsentDeptCodes：请求内编码重复时抛出 DATA_CODE_DUPLICATE")
	void requireAbsentDeptCodesThrowsWhenDuplicatedInRequest() {
		ThrowingCallable executable = () -> deptReferenceChecker.requireAbsentDeptCodes(List.of("RD", "RD"));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysDeptMapper, never()).selectActiveByDeptCodes(any());
	}

	@Test
	@DisplayName("requireAbsentDeptCodes：库中已存在编码时抛出 DATA_CODE_DUPLICATE")
	void requireAbsentDeptCodesThrowsWhenDeptCodeExists() {
		SysDeptEntity existing = activeDept();
		existing.setDeptCode("RD");
		when(sysDeptMapper.selectActiveByDeptCodes(List.of("RD"))).thenReturn(List.of(existing));

		ThrowingCallable executable = () -> deptReferenceChecker.requireAbsentDeptCodes(List.of("RD"));
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_CODE_DUPLICATE);

		verify(sysDeptMapper).selectActiveByDeptCodes(List.of("RD"));
	}

	@Test
	@DisplayName("requireAbsentDeptCodes：编码均未占用时不抛异常")
	void requireAbsentDeptCodesPassesWhenAllAbsent() {
		when(sysDeptMapper.selectActiveByDeptCodes(List.of("RD", "OPS"))).thenReturn(List.of());

		assertThatCode(() -> deptReferenceChecker.requireAbsentDeptCodes(List.of("RD", "OPS")))
			.doesNotThrowAnyException();

		verify(sysDeptMapper).selectActiveByDeptCodes(List.of("RD", "OPS"));
	}

}
