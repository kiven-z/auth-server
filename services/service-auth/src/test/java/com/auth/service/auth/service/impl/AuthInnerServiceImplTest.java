package com.auth.service.auth.service.impl;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.infrastructure.profile.AuthProfileCache;
import com.auth.service.auth.infrastructure.profile.AuthProfileService;
import com.auth.service.auth.infrastructure.session.UserSessionIndexSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * AuthInnerServiceImpl 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class AuthInnerServiceImplTest {

	@Mock
	private UserSessionIndexSession userSessionIndexSession;

	@Mock
	private AuthProfileService authProfileService;

	@Mock
	private AuthProfileCache authProfileCache;

	@Test
	@DisplayName("刷新缓存：入参为空/含 null 时不抛异常且跳过 null")
	void refreshUserCache_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		// Arrange
		AuthInnerServiceImpl service = new AuthInnerServiceImpl(userSessionIndexSession, authProfileService,
				authProfileCache);

		// empty
		assertDoesNotThrow(() -> service.refreshUserCache(List.of()));
		verifyNoInteractions(authProfileService, authProfileCache);

		// contains null + exception
		AuthProfile profile = mock(AuthProfile.class);
		when(authProfileService.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);
		doThrow(new RuntimeException("boom")).when(authProfileCache).createCacheAuthProfile(profile);

		assertDoesNotThrow(() -> service.refreshUserCache(Arrays.asList(null, TestConstants.USER_ID)));
		verify(authProfileService).buildByUserId(TestConstants.USER_ID);
		verify(authProfileCache).createCacheAuthProfile(profile);
	}

	@Test
	@DisplayName("踢会话：入参为空/含 null 时不抛异常且跳过 null；下游异常会被吞掉")
	void kickAllSessions_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		// Arrange
		AuthInnerServiceImpl service = new AuthInnerServiceImpl(userSessionIndexSession, authProfileService,
				authProfileCache);

		// empty
		assertDoesNotThrow(() -> service.kickAllSessions(List.of()));
		verifyNoInteractions(userSessionIndexSession);

		// contains null + exception
		doThrow(new RuntimeException("boom")).when(userSessionIndexSession).deleteAllSessions(TestConstants.USER_ID);
		assertDoesNotThrow(() -> service.kickAllSessions(Arrays.asList(null, TestConstants.USER_ID)));
		verify(userSessionIndexSession).deleteAllSessions(TestConstants.USER_ID);
	}

}
