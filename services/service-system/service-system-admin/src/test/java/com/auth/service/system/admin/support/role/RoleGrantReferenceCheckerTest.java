package com.auth.service.system.admin.support.role;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link RoleGrantReferenceChecker} 单元测试。
 */
@DisplayName("RoleGrantReferenceChecker grant 角色校验")
@ExtendWith(MockitoExtension.class)
class RoleGrantReferenceCheckerTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@InjectMocks
	private RoleGrantReferenceChecker roleGrantReferenceChecker;

	@Test
	@DisplayName("requireExistingEnabledRoleIds 可指定结果码")
	void requireExistingEnabledRoleIdsUsesCustomResultCode() {
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatCode(() -> roleGrantReferenceChecker.requireExistingEnabledRoleIds(List.of(1L),
				SystemCommonResultCode.DATA_NOT_EXIST))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("按 ID：空列表跳过校验")
	void requireExistingEnabledRoleIdsSkipsWhenEmpty() {
		assertThatCode(() -> roleGrantReferenceChecker.requireExistingEnabledRoleIds(List.of(),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.doesNotThrowAnyException();

		verify(sysRoleMapper, never()).selectCount(any());
	}

	@Test
	@DisplayName("按 ID：全部 eligible 时不抛异常")
	void requireExistingEnabledRoleIdsPassesWhenAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatCode(() -> roleGrantReferenceChecker.requireExistingEnabledRoleIds(List.of(1L),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("按 ID：存在无效角色时抛出指定结果码")
	void requireExistingEnabledRoleIdsThrowsWhenNotAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);

		List<Long> roleIds = List.of(99L);
		assertThatThrownBy(() -> roleGrantReferenceChecker.requireExistingEnabledRoleIds(roleIds,
				SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

	@Test
	@DisplayName("grant_table 按 ID：空列表跳过校验")
	void requireGrantTableOrgRoleIdsSkipsWhenEmpty() {
		assertThatCode(
				() -> roleGrantReferenceChecker.requireGrantTableOrgRoleIds(GrantTableSubjectType.DEPT, List.of()))
			.doesNotThrowAnyException();

		verify(sysRoleMapper, never()).selectCount(any());
	}

	@Test
	@DisplayName("grant_table 按 ID：全部 eligible 时不抛异常")
	void requireGrantTableOrgRoleIdsPassesWhenAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(2L);

		assertThatCode(() -> roleGrantReferenceChecker.requireGrantTableOrgRoleIds(GrantTableSubjectType.DEPT,
				List.of(101L, 102L)))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("grant_table 按 ID：存在无效角色时抛出 GRANT_REFERENCE_INVALID")
	void requireGrantTableOrgRoleIdsThrowsWhenNotAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);

		List<Long> roleIds = List.of(101L);
		assertThatThrownBy(
				() -> roleGrantReferenceChecker.requireGrantTableOrgRoleIds(GrantTableSubjectType.POST, roleIds))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

}
