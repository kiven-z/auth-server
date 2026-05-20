package com.auth.service.auth.integration.application;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authorization.api.dto.EffectiveCodesVO;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.authorization.application.AuthProfileCacheApplicationService;
import com.auth.service.auth.session.application.SessionAdminApplicationService;
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
 * {@link InternalAuthCoordinationService} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class InternalAuthCoordinationServiceTest {

	@Mock
	private SessionAdminApplicationService sessionAdminApplicationService;

	@Mock
	private AuthProfileQueryService authProfileQueryService;

	@Mock
	private AuthProfileCacheApplicationService authProfileCacheApplicationService;

	@Test
	@DisplayName("刷新缓存：入参为空/含 null 时不抛异常且跳过 null")
	void refreshUserCache_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		InternalAuthCoordinationService service = new InternalAuthCoordinationService(sessionAdminApplicationService,
				authProfileQueryService, authProfileCacheApplicationService);

		assertDoesNotThrow(() -> service.refreshUserCache(List.of()));
		verifyNoInteractions(authProfileQueryService, authProfileCacheApplicationService);

		AuthProfile profile = mock(AuthProfile.class);
		when(authProfileQueryService.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);
		doThrow(new RuntimeException("boom")).when(authProfileCacheApplicationService).cacheProfile(profile);

		assertDoesNotThrow(() -> service.refreshUserCache(Arrays.asList(null, TestConstants.USER_ID)));
		verify(authProfileQueryService).buildByUserId(TestConstants.USER_ID);
		verify(authProfileCacheApplicationService).cacheProfile(profile);
	}

	@Test
	@DisplayName("踢会话：入参为空/含 null 时不抛异常且跳过 null；下游异常会被吞掉")
	void kickAllSessions_shouldIgnoreEmptyAndNullAndSwallowExceptions() {
		InternalAuthCoordinationService service = new InternalAuthCoordinationService(sessionAdminApplicationService,
				authProfileQueryService, authProfileCacheApplicationService);

		assertDoesNotThrow(() -> service.kickAllSessions(List.of()));
		verifyNoInteractions(sessionAdminApplicationService);

		doThrow(new RuntimeException("boom")).when(sessionAdminApplicationService)
			.kickAllSessions(TestConstants.USER_ID);
		assertDoesNotThrow(() -> service.kickAllSessions(Arrays.asList(null, TestConstants.USER_ID)));
		verify(sessionAdminApplicationService).kickAllSessions(TestConstants.USER_ID);
	}

	@Test
	@DisplayName("resolveEffectiveCodes：委托 AuthProfileQueryService 并拷贝角色与权限列表")
	void resolveEffectiveCodes_mapsProfileRolesAndPermissions() {
		InternalAuthCoordinationService service = new InternalAuthCoordinationService(sessionAdminApplicationService,
				authProfileQueryService, authProfileCacheApplicationService);
		AuthProfile profile = AuthProfile.builder()
			.userId(TestConstants.USER_ID)
			.username("u1")
			.roles(List.of("ADMIN"))
			.permissions(List.of("*"))
			.build();
		when(authProfileQueryService.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);

		EffectiveCodesVO vo = service.resolveEffectiveCodes(TestConstants.USER_ID);

		assertEquals(List.of("ADMIN"), vo.getRoleCodes());
		assertEquals(List.of("*"), vo.getPermissionCodes());
		verify(authProfileQueryService).buildByUserId(TestConstants.USER_ID);
		verifyNoInteractions(authProfileCacheApplicationService);
	}

}
