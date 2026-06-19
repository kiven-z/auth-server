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
import static org.mockito.Mockito.*;

/**
 * {@link PermissionReferenceChecker} 单元测试。
 */
@DisplayName("PermissionReferenceChecker 权限码校验")
@ExtendWith(MockitoExtension.class)
class PermissionReferenceCheckerTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@InjectMocks
	private PermissionReferenceChecker permissionReferenceChecker;

	@Test
	@DisplayName("空列表跳过校验")
	void requireActivePermissionCodesSkipsWhenEmpty() {
		// 空入参不应访问权限表
		assertThatCode(() -> permissionReferenceChecker.requireActivePermissionCodes(List.of()))
			.doesNotThrowAnyException();

		verify(sysPermissionMapper, never()).selectCount(any());
	}

	@Test
	@DisplayName("全部已启用时不抛异常")
	void requireActivePermissionCodesPassesWhenAllActive() {
		when(sysPermissionMapper.selectCount(any())).thenReturn(2L);

		assertThatCode(() -> permissionReferenceChecker
			.requireActivePermissionCodes(List.of("sys:user:list", "sys:user:query"))).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("存在无效或未启用权限码时抛出 ROLE_PERMISSION_INVALID")
	void requireActivePermissionCodesThrowsWhenNotAllActive() {
		when(sysPermissionMapper.selectCount(any())).thenReturn(0L);

		List<String> permissionCodes = List.of("missing:perm");
		assertThatThrownBy(() -> permissionReferenceChecker.requireActivePermissionCodes(permissionCodes))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
	}

}
