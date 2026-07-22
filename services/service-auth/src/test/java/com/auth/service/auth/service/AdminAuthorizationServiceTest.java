package com.auth.service.auth.service;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.model.constants.AuthBatchSizes;
import com.auth.service.auth.model.vo.authorization.EffectiveCodesVO;
import com.auth.service.auth.service.impl.AdminAuthorizationServiceImpl;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.invalidation.AuthProfileMaterializationService;
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
	private AuthProfileMaterializationService authProfileMaterializationService;

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
	@DisplayName("刷新缓存：入参为空/含 null 时委托物化服务分批刷新")
	void refreshUserCache_shouldDelegateToMaterializationService() {
		assertDoesNotThrow(() -> adminAuthorizationService.refreshUserCache(List.of()));
		verifyNoInteractions(authProfileMaterializationService);

		adminAuthorizationService.refreshUserCache(Arrays.asList(null, TestConstants.USER_ID));

		verify(authProfileMaterializationService).refreshInBatches(List.of(TestConstants.USER_ID),
				AuthBatchSizes.INVALIDATION);
	}

}
