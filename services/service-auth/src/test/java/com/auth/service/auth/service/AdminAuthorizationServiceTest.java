package com.auth.service.auth.service;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.model.vo.authorization.EffectiveCodesVO;
import com.auth.service.auth.service.impl.AdminAuthorizationServiceImpl;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.redis.AuthProfileRedisCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link AdminAuthorizationService} 单元测试
 */
@DisplayName("AdminAuthorizationService 管理端授权")
@ExtendWith(MockitoExtension.class)
class AdminAuthorizationServiceTest {

	@Mock
	private AuthProfileRepository authProfileRepository;

	@Mock
	private AuthProfileRedisCache authProfileRedisCache;

	@InjectMocks
	private AdminAuthorizationServiceImpl adminAuthorizationService;

	@Test
	@DisplayName("加载授权画像：委托 AuthProfileRepository")
	void loadAuthProfile_shouldDelegateToRepository() {
		AuthProfile profile = mock(AuthProfile.class);
		when(authProfileRepository.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);

		AuthProfile loaded = adminAuthorizationService.loadAuthProfile(TestConstants.USER_ID);

		assertSame(profile, loaded);
		verify(authProfileRepository).buildByUserId(TestConstants.USER_ID);
	}

	@Test
	@DisplayName("查询生效码：委托 AuthProfileRepository 并映射为 VO")
	void getEffectiveCodes_shouldDelegateToRepositoryAndMapToVo() {
		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getRoles()).thenReturn(List.of("ADMIN"));
		when(profile.getPermissions()).thenReturn(List.of("auth:user:read"));
		when(authProfileRepository.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);

		EffectiveCodesVO effectiveCodes = adminAuthorizationService.getEffectiveCodes(TestConstants.USER_ID);

		assertEquals(List.of("ADMIN"), effectiveCodes.getRoleCodes());
		assertEquals(List.of("auth:user:read"), effectiveCodes.getPermissionCodes());
		verify(authProfileRepository).buildByUserId(TestConstants.USER_ID);
	}

	@Test
	@DisplayName("查询生效码：画像为 null 时返回空列表")
	void getEffectiveCodes_shouldReturnEmptyListsWhenProfileNull() {
		when(authProfileRepository.buildByUserId(TestConstants.USER_ID)).thenReturn(null);

		EffectiveCodesVO effectiveCodes = adminAuthorizationService.getEffectiveCodes(TestConstants.USER_ID);

		assertEquals(List.of(), effectiveCodes.getRoleCodes());
		assertEquals(List.of(), effectiveCodes.getPermissionCodes());
	}

	@Test
	@DisplayName("刷新缓存：入参为空/含 null 时不抛异常且跳过 null")
	void refreshUserCache_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		assertDoesNotThrow(() -> adminAuthorizationService.refreshUserCache(List.of()));
		verifyNoInteractions(authProfileRepository, authProfileRedisCache);

		AuthProfile profile = mock(AuthProfile.class);
		when(authProfileRepository.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);
		doThrow(new RuntimeException("boom")).when(authProfileRedisCache).cacheProfile(profile);

		assertDoesNotThrow(
				() -> adminAuthorizationService.refreshUserCache(Arrays.asList(null, TestConstants.USER_ID)));
		verify(authProfileRepository).buildByUserId(TestConstants.USER_ID);
		verify(authProfileRedisCache).cacheProfile(profile);
	}

}
