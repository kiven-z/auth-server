package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authorization.api.dto.EffectiveCodesVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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
	@DisplayName("resolveEffectiveCodes：委托 AuthProfileQueryService 并拷贝角色与权限列表")
	void resolveEffectiveCodes_mapsProfileRolesAndPermissions() {
		AuthProfile profile = AuthProfile.builder()
			.userId(TestConstants.USER_ID)
			.username("u1")
			.roles(List.of("ADMIN"))
			.permissions(List.of("*"))
			.build();
		when(authProfileQueryService.buildByUserId(TestConstants.USER_ID)).thenReturn(profile);

		EffectiveCodesVO vo = authorizationInnerService.resolveEffectiveCodes(TestConstants.USER_ID);

		assertEquals(List.of("ADMIN"), vo.getRoleCodes());
		assertEquals(List.of("*"), vo.getPermissionCodes());
		verify(authProfileQueryService).buildByUserId(TestConstants.USER_ID);
		verifyNoMoreInteractions(authProfileQueryService);
	}

}
