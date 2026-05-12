package com.auth.service.auth.service.impl;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.infrastructure.profile.AuthProfileCache;
import com.auth.service.auth.infrastructure.profile.DefaultAuthProfileServiceImpl;
import com.auth.service.auth.infrastructure.session.redis.RedisUserSessionIndexRepository;
import com.auth.service.auth.model.vo.inner.EffectiveCodesVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * AuthInnerServiceImpl 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class InternalAuthCoordinationServiceImplTest {

	@Mock
	private RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	@Mock
	private DefaultAuthProfileServiceImpl defaultAuthProfileServiceImpl;

	@Mock
	private AuthProfileCache authProfileCache;

	@Test
	@DisplayName("刷新缓存：入参为空/含 null 时不抛异常且跳过 null")
	void refreshUserCache_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		InternalAuthCoordinationServiceImpl service = new InternalAuthCoordinationServiceImpl(
				redisUserSessionIndexRepository, defaultAuthProfileServiceImpl, authProfileCache);

		assertDoesNotThrow(() -> service.refreshUserCache(List.of()));
		verifyNoInteractions(defaultAuthProfileServiceImpl, authProfileCache);

		AuthProfile profile = mock(AuthProfile.class);
		when(defaultAuthProfileServiceImpl.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);
		doThrow(new RuntimeException("boom")).when(authProfileCache).createCacheAuthProfile(profile);

		assertDoesNotThrow(() -> service.refreshUserCache(Arrays.asList(null, TestConstants.USER_ID)));
		verify(defaultAuthProfileServiceImpl).buildByUserId(TestConstants.USER_ID);
		verify(authProfileCache).createCacheAuthProfile(profile);
	}

	@Test
	@DisplayName("踢会话：入参为空/含 null 时不抛异常且跳过 null；下游异常会被吞掉")
	void kickAllSessions_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		InternalAuthCoordinationServiceImpl service = new InternalAuthCoordinationServiceImpl(
				redisUserSessionIndexRepository, defaultAuthProfileServiceImpl, authProfileCache);

		assertDoesNotThrow(() -> service.kickAllSessions(List.of()));
		verifyNoInteractions(redisUserSessionIndexRepository);

		doThrow(new RuntimeException("boom")).when(redisUserSessionIndexRepository)
			.deleteAllSessions(TestConstants.USER_ID);
		assertDoesNotThrow(() -> service.kickAllSessions(Arrays.asList(null, TestConstants.USER_ID)));
		verify(redisUserSessionIndexRepository).deleteAllSessions(TestConstants.USER_ID);
	}

	@Test
	@DisplayName("resolveEffectiveCodes：委托 AuthProfileService 并拷贝角色与权限列表")
	void resolveEffectiveCodes_mapsProfileRolesAndPermissions() {
		InternalAuthCoordinationServiceImpl service = new InternalAuthCoordinationServiceImpl(
				redisUserSessionIndexRepository, defaultAuthProfileServiceImpl, authProfileCache);
		AuthProfile profile = AuthProfile.builder()
			.userId(TestConstants.USER_ID)
			.username("u1")
			.roles(List.of("ADMIN"))
			.permissions(List.of("*"))
			.build();
		when(defaultAuthProfileServiceImpl.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);

		EffectiveCodesVO vo = service.resolveEffectiveCodes(TestConstants.USER_ID);

		assertEquals(List.of("ADMIN"), vo.getRoleCodes());
		assertEquals(List.of("*"), vo.getPermissionCodes());
		verify(defaultAuthProfileServiceImpl).buildByUserId(TestConstants.USER_ID);
		verifyNoInteractions(authProfileCache);
	}

}
