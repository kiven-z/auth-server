package com.auth.service.auth.authentication.application.login.audit;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.TestConstants;
import com.auth.service.auth.authentication.application.port.AuthProfileCachePort;
import com.auth.service.auth.authorization.domain.model.UserAccountSnapshot;
import com.auth.service.auth.authorization.domain.port.UserReadPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link LoginAuditPrincipalNormalizer} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class LoginAuditPrincipalNormalizerTest {

	@Mock
	private AuthProfileCachePort authProfileCachePort;

	@Mock
	private UserReadPort userReadPort;

	private LoginAuditPrincipalNormalizer newNormalizer() {
		return new LoginAuditPrincipalNormalizer(authProfileCachePort, userReadPort);
	}

	@Test
	@DisplayName("无 userId：保留登录尝试主体")
	void normalize_shouldKeepAttemptPrincipal_whenUserIdNull() {
		LoginAuditPrincipalNormalizer normalizer = newNormalizer();

		assertEquals("13800138000", normalizer.normalize(null, "13800138000"));
		assertNull(normalizer.normalize(null, "  "));
		verifyNoInteractions(authProfileCachePort, userReadPort);
	}

	@Test
	@DisplayName("有 userId 且入参合法：保留入参登录名")
	void normalize_shouldKeepIncoming_whenUsablePrincipal() {
		LoginAuditPrincipalNormalizer normalizer = newNormalizer();

		assertEquals("Administrator", normalizer.normalize(TestConstants.USER_ID, "Administrator"));
		verifyNoInteractions(authProfileCachePort, userReadPort);
	}

	@Test
	@DisplayName("有 userId 且入参为历史占位符：从画像缓存解析登录名")
	void normalize_shouldResolveFromCache_whenPlaceholderPrincipal() {
		AuthProfile profile = AuthProfile.builder().userId(TestConstants.USER_ID).username("Administrator").build();
		when(authProfileCachePort.loadCachedProfile(TestConstants.USER_ID)).thenReturn(Optional.of(profile));

		LoginAuditPrincipalNormalizer normalizer = newNormalizer();

		assertEquals("Administrator", normalizer.normalize(TestConstants.USER_ID, "/"));
		verifyNoInteractions(userReadPort);
	}

	@Test
	@DisplayName("有 userId 且缓存未命中：从 UserReadPort 解析登录名")
	void normalize_shouldResolveFromDb_whenCacheMiss() {
		when(authProfileCachePort.loadCachedProfile(TestConstants.USER_ID)).thenReturn(Optional.empty());
		when(userReadPort.findById(TestConstants.USER_ID))
			.thenReturn(Optional.of(new UserAccountSnapshot(TestConstants.USER_ID, "Administrator", 1L)));

		LoginAuditPrincipalNormalizer normalizer = newNormalizer();

		assertEquals("Administrator", normalizer.normalize(TestConstants.USER_ID, null));
	}

}
