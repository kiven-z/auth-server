package com.auth.service.system.admin.support.permission;

import com.auth.service.system.admin.mapper.permission.SysPermissionMapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PermissionReferenceChecker} 单元测试。
 */
@DisplayName("PermissionReferenceChecker 权限 ID 校验")
@ExtendWith(MockitoExtension.class)
class PermissionReferenceCheckerTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@InjectMocks
	private PermissionReferenceChecker permissionReferenceChecker;

	@Test
	@DisplayName("空列表跳过校验")
	void requireActivePermissionIdsSkipsWhenEmpty() {
		assertThatCode(() -> permissionReferenceChecker.requireActivePermissionIds(List.of()))
			.doesNotThrowAnyException();

		verify(sysPermissionMapper, never()).selectCount(any());
	}

	@Test
	@DisplayName("全部已启用时不抛异常")
	void requireActivePermissionIdsPassesWhenAllActive() {
		when(sysPermissionMapper.selectCount(any())).thenReturn(2L);

		assertThatCode(() -> permissionReferenceChecker.requireActivePermissionIds(List.of(10L, 20L)))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("存在无效或未启用权限 ID 时抛出 GRANT_REFERENCE_INVALID")
	void requireActivePermissionIdsThrowsWhenNotAllActive() {
		when(sysPermissionMapper.selectCount(any())).thenReturn(0L);

		List<Long> permissionIds = List.of(99L);
		assertThatThrownBy(() -> permissionReferenceChecker.requireActivePermissionIds(permissionIds))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

}
