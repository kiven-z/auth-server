package com.auth.service.auth.application.authorization;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * {@link AdminAuthorizationService} 单元测试
 */
@DisplayName("AdminAuthorizationService 管理端授权")
@ExtendWith(MockitoExtension.class)
class AdminAuthorizationServiceTest {

	@Mock
	private AuthProfileQueryService authProfileQueryService;

	@Mock
	private AuthProfileCacheApplicationService authProfileCacheApplicationService;

	@InjectMocks
	private AdminAuthorizationService adminAuthorizationService;

	@Test
	@DisplayName("刷新缓存：入参为空/含 null 时不抛异常且跳过 null")
	void refreshUserCache_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		// 空列表直接返回
		assertDoesNotThrow(() -> adminAuthorizationService.refreshUserCache(List.of()));
		verifyNoInteractions(authProfileQueryService, authProfileCacheApplicationService);

		AuthProfile profile = mock(AuthProfile.class);
		when(authProfileQueryService.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);
		doThrow(new RuntimeException("boom")).when(authProfileCacheApplicationService).cacheProfile(profile);

		// 含 null 时跳过 null，单用户失败不向外抛
		assertDoesNotThrow(
				() -> adminAuthorizationService.refreshUserCache(Arrays.asList(null, TestConstants.USER_ID)));
		verify(authProfileQueryService).buildByUserId(TestConstants.USER_ID);
		verify(authProfileCacheApplicationService).cacheProfile(profile);
	}

}
