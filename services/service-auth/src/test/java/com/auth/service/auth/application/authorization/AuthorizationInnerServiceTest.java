package com.auth.service.auth.application.authorization;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInnerService} 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationInnerServiceTest {

	@Mock
	private AuthProfileQueryService authProfileQueryService;

	@InjectMocks
	private AuthorizationInnerService authorizationInnerService;

	@Test
	@DisplayName("resolveAuthProfile：委托 AuthProfileQueryService")
	void resolveAuthProfile_shouldDelegateToQueryService() {
		AuthProfile profile = AuthProfile.builder()
			.userId(TestConstants.USER_ID)
			.username("u1")
			.roles(List.of("ADMIN"))
			.permissions(List.of("*"))
			.build();
		when(authProfileQueryService.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);

		AuthProfile result = authorizationInnerService.resolveAuthProfile(TestConstants.USER_ID);

		assertEquals(profile, result);
		verify(authProfileQueryService).buildByUserId(TestConstants.USER_ID);
		verifyNoMoreInteractions(authProfileQueryService);
	}

}
