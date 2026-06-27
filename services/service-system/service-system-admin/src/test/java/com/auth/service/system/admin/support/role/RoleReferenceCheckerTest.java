package com.auth.service.system.admin.support.role;

import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RoleReferenceChecker} 单元测试。
 */
@DisplayName("RoleReferenceChecker 角色关联校验")
@ExtendWith(MockitoExtension.class)
class RoleReferenceCheckerTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@InjectMocks
	private RoleReferenceChecker roleReferenceChecker;

	@Test
	@DisplayName("角色 ID 为空时抛出 DATA_NOT_EXIST")
	void getExistingActiveThrowsWhenNull() {
		when(sysRoleMapper.selectById(null)).thenReturn(null);

		assertThatThrownBy(() -> roleReferenceChecker.getExistingActive(null))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("角色不存在时抛出 DATA_NOT_EXIST")
	void getExistingActiveThrowsWhenMissing() {
		when(sysRoleMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> roleReferenceChecker.getExistingActive(99L))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("角色存在时返回实体")
	void getExistingActiveReturnsEntity() {
		SysRoleEntity role = new SysRoleEntity();
		role.setId(1L);
		role.setRoleCode("ADMIN");
		when(sysRoleMapper.selectById(1L)).thenReturn(role);

		SysRoleEntity result = roleReferenceChecker.getExistingActive(1L);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getRoleCode()).isEqualTo("ADMIN");
	}

	@Test
	@DisplayName("requireExistingEnabledRoleIds 可指定结果码")
	void requireExistingEnabledRoleIdsUsesCustomResultCode() {
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatCode(() -> roleReferenceChecker.requireExistingEnabledRoleIds(List.of(1L),
				SystemCommonResultCode.DATA_NOT_EXIST))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("按 ID：空列表跳过校验")
	void requireExistingEnabledRoleIdsSkipsWhenEmpty() {
		assertThatCode(() -> roleReferenceChecker.requireExistingEnabledRoleIds(List.of(),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.doesNotThrowAnyException();

		verify(sysRoleMapper, never()).selectCount(any());
	}

	@Test
	@DisplayName("按 ID：全部 eligible 时不抛异常")
	void requireExistingEnabledRoleIdsPassesWhenAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(1L);

		assertThatCode(() -> roleReferenceChecker.requireExistingEnabledRoleIds(List.of(1L),
				SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("按 ID：存在无效角色时抛出指定结果码")
	void requireExistingEnabledRoleIdsThrowsWhenNotAllEligible() {
		when(sysRoleMapper.selectCount(any())).thenReturn(0L);

		List<Long> roleIds = List.of(99L);
		assertThatThrownBy(() -> roleReferenceChecker.requireExistingEnabledRoleIds(roleIds,
				SystemCommonResultCode.GRANT_REFERENCE_INVALID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

}
