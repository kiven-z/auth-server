package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * {@link DeptReferenceChecker} 单元测试。
 */
@DisplayName("DeptReferenceChecker 部门引用校验")
@ExtendWith(MockitoExtension.class)
class DeptReferenceCheckerTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	@InjectMocks
	private DeptReferenceChecker deptReferenceChecker;

	@Test
	@DisplayName("normalizeParentId：null 与负数归一为顶级")
	void normalizeParentIdMapsNullAndNegativeToRoot() {
		assertThat(DeptReferenceChecker.normalizeParentId(null)).isZero();
		assertThat(DeptReferenceChecker.normalizeParentId(-1L)).isZero();
		assertThat(DeptReferenceChecker.normalizeParentId(5L)).isEqualTo(5L);
	}

	@Test
	@DisplayName("resolveParentId：委托 normalizeParentId")
	void resolveParentIdDelegatesToNormalize() {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setParentId(null);
		assertThat(DeptReferenceChecker.resolveParentId(entity)).isZero();

		entity.setParentId(-2L);
		assertThat(DeptReferenceChecker.resolveParentId(entity)).isZero();

		entity.setParentId(3L);
		assertThat(DeptReferenceChecker.resolveParentId(entity)).isEqualTo(3L);
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
		when(sysDeptMapper.countActiveById(8L)).thenReturn(1);

		assertThat(deptReferenceChecker.normalizeAndRequireActiveParent(8L)).isEqualTo(8L);
		verify(sysDeptMapper).countActiveById(8L);
	}

	@Test
	@DisplayName("normalizeAndRequireActiveParent：父部门不存在时抛出 DEPT_PARENT_NOT_FOUND")
	void normalizeAndRequireActiveParentThrowsWhenParentMissing() {
		when(sysDeptMapper.countActiveById(9L)).thenReturn(0);

		assertThatThrownBy(() -> deptReferenceChecker.normalizeAndRequireActiveParent(9L))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemAdminResultCode.DEPT_PARENT_NOT_FOUND);
	}

}
