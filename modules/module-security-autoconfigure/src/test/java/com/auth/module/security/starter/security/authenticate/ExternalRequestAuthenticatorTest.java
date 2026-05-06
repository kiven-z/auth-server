package com.auth.module.security.starter.security.authenticate;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.autoconfig.config.security.SecurityConfigProperties;
import com.auth.module.security.autoconfig.config.security.VersionMismatchStrategy;
import com.auth.module.security.autoconfig.pipeline.authenticate.ExternalRequestAuthenticator;
import com.auth.module.security.autoconfig.pipeline.authenticate.SessionCountChecker;
import com.auth.module.security.autoconfig.service.AuthProfileCacheService;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.facade.SecurityTokenHandlerFacade;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ExternalRequestAuthenticatorTest {

    private SecurityTokenHandlerFacade tokenHandlerFacade;

    private AuthProfileCacheService authProfileCacheService;

    private SessionCountChecker sessionCountChecker;

    private SecurityConfigProperties securityConfigProperties;

    private ExternalRequestAuthenticator authenticator;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        tokenHandlerFacade = mock(SecurityTokenHandlerFacade.class);
        authProfileCacheService = mock(AuthProfileCacheService.class);
        sessionCountChecker = mock(SessionCountChecker.class);
        securityConfigProperties = new SecurityConfigProperties();
        authenticator = new ExternalRequestAuthenticator(tokenHandlerFacade, authProfileCacheService,
                sessionCountChecker, securityConfigProperties);
        request = mock(HttpServletRequest.class);
        when(request.getHeader(SecurityInternalTokenConstants.INTERNAL_HEADER)).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid.jwt");
    }

    @Test
    @DisplayName("测试支持内部头部")
    void supports_withInternalHeader_returnsFalse() {
        when(request.getHeader(SecurityInternalTokenConstants.INTERNAL_HEADER)).thenReturn("some.internal.jwt");
        assertFalse(authenticator.supports(request));
    }

    @Test
    @DisplayName("测试支持授权头部")
    void supports_withAuthorizationHeader_returnsTrue() {
        assertTrue(authenticator.supports(request));
    }

    @Test
    @DisplayName("测试不支持授权头部")
    void supports_withoutAuthorizationHeader_returnsFalse() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        assertFalse(authenticator.supports(request));
    }

    @Test
    @DisplayName("测试认证成功")
    void authenticate_success() {
        SecurityTokenResult result = SecurityTokenResult.builder()
                .kind(SecurityTokenKind.EXTERNAL_ACCESS)
                .rawToken("valid.jwt")
                .userToken(JwtUserToken.builder().userId(100L).jti("jti-1").build())
                .permVersion(1L)
                .build();
        when(tokenHandlerFacade.validateExternal("Bearer valid.jwt")).thenReturn(result);

        AuthProfile profile = AuthProfile.builder().userId(100L).username("alice").permVersion(1L).build();
        when(authProfileCacheService.load(100L)).thenReturn(profile);

        doNothing().when(sessionCountChecker).verifySessionPresent("jti-1");
        doNothing().when(sessionCountChecker).verifyConcurrentSessionsWithinLimit(100L, "jti-1");

        AuthProfile resultProfile = authenticator.authenticate(request);
        assertNotNull(resultProfile);
        assertEquals(100L, resultProfile.getUserId());
        verify(sessionCountChecker).verifySessionPresent("jti-1");
        verify(sessionCountChecker).verifyConcurrentSessionsWithinLimit(100L, "jti-1");
    }

    @Test
    @DisplayName("测试权限版本不匹配时需要刷新")
    void authenticate_permVersionMismatch_REQUIRE_REFRESH() {
        securityConfigProperties.setVersionMismatchStrategy(VersionMismatchStrategy.REQUIRE_REFRESH);

        SecurityTokenResult result = SecurityTokenResult.builder()
                .kind(SecurityTokenKind.EXTERNAL_ACCESS)
                .rawToken("old.jwt")
                .userToken(JwtUserToken.builder().userId(100L).jti("jti-1").build())
                .permVersion(1L)
                .build();
        when(tokenHandlerFacade.validateExternal(anyString())).thenReturn(result);

        AuthProfile profile = AuthProfile.builder().userId(100L).username("alice").permVersion(2L).build();
        when(authProfileCacheService.load(100L)).thenReturn(profile);

        SecurityTokenException ex = assertThrows(SecurityTokenException.class,
                () -> authenticator.authenticate(request));
        assertEquals(SecurityResultCodeEnum.PERMISSION_VERSION_MISMATCH, ex.getResultCode());
    }

    @Test
    @DisplayName("测试权限版本不匹配时拒绝")
    void authenticate_permVersionMismatch_DENY() {
        securityConfigProperties.setVersionMismatchStrategy(VersionMismatchStrategy.DENY);

        SecurityTokenResult result = SecurityTokenResult.builder()
                .kind(SecurityTokenKind.EXTERNAL_ACCESS)
                .rawToken("old.jwt")
                .userToken(JwtUserToken.builder().userId(100L).jti("jti-1").build())
                .permVersion(1L)
                .build();
        when(tokenHandlerFacade.validateExternal(anyString())).thenReturn(result);

        AuthProfile profile = AuthProfile.builder().userId(100L).username("alice").permVersion(2L).build();
        when(authProfileCacheService.load(100L)).thenReturn(profile);

        SecurityTokenException ex = assertThrows(SecurityTokenException.class,
                () -> authenticator.authenticate(request));
        assertEquals(SecurityResultCodeEnum.ACCESS_DENIED, ex.getResultCode());
    }

    @Test
    @DisplayName("测试权限版本不匹配时强制重新认证")
    void authenticate_permVersionMismatch_FORCE_REAUTH() {
        securityConfigProperties.setVersionMismatchStrategy(VersionMismatchStrategy.FORCE_REAUTH);

        SecurityTokenResult result = SecurityTokenResult.builder()
                .kind(SecurityTokenKind.EXTERNAL_ACCESS)
                .rawToken("old.jwt")
                .userToken(JwtUserToken.builder().userId(100L).jti("jti-1").build())
                .permVersion(1L)
                .build();
        when(tokenHandlerFacade.validateExternal(anyString())).thenReturn(result);

        AuthProfile profile = AuthProfile.builder().userId(100L).username("alice").permVersion(2L).build();
        when(authProfileCacheService.load(100L)).thenReturn(profile);

        SecurityTokenException ex = assertThrows(SecurityTokenException.class,
                () -> authenticator.authenticate(request));
        assertEquals(SecurityResultCodeEnum.SESSION_DISABLED, ex.getResultCode());
    }

    @Test
    @DisplayName("测试用户信息缺失时抛出会话缺失异常")
    void authenticate_profileMissing_throwsSessionMissing() {
        SecurityTokenResult result = SecurityTokenResult.builder()
                .kind(SecurityTokenKind.EXTERNAL_ACCESS)
                .rawToken("valid.jwt")
                .userToken(JwtUserToken.builder().userId(404L).jti("jti-404").build())
                .build();
        when(tokenHandlerFacade.validateExternal(anyString())).thenReturn(result);
        when(authProfileCacheService.load(404L)).thenReturn(null);

        SecurityTokenException ex = assertThrows(SecurityTokenException.class,
                () -> authenticator.authenticate(request));
        assertEquals(SecurityResultCodeEnum.SESSION_MISSING, ex.getResultCode());
    }

    @Test
    @DisplayName("测试用户ID缺失时抛出令牌无效异常")
    void authenticate_userIdMissing_throwsTokenInvalid() {
        SecurityTokenResult result = SecurityTokenResult.builder()
                .kind(SecurityTokenKind.EXTERNAL_ACCESS)
                .rawToken("no-user.jwt")
                .userToken(JwtUserToken.builder().build())
                .build();
        when(tokenHandlerFacade.validateExternal(anyString())).thenReturn(result);

        SecurityTokenException ex = assertThrows(SecurityTokenException.class,
                () -> authenticator.authenticate(request));
        assertEquals(SecurityResultCodeEnum.TOKEN_INVALID, ex.getResultCode());
    }

    @Test
    @DisplayName("测试会话不存在时抛出会话缺失异常")
    void authenticate_sessionNotPresent_throwsSessionMissing() {
        SecurityTokenResult result = SecurityTokenResult.builder()
                .kind(SecurityTokenKind.EXTERNAL_ACCESS)
                .rawToken("valid.jwt")
                .userToken(JwtUserToken.builder().userId(100L).jti("jti-expired").build())
                .build();
        when(tokenHandlerFacade.validateExternal(anyString())).thenReturn(result);

        doThrow(new SecurityTokenException(SecurityResultCodeEnum.SESSION_MISSING, "Session is missing."))
                .when(sessionCountChecker)
                .verifySessionPresent("jti-expired");

        SecurityTokenException ex = assertThrows(SecurityTokenException.class,
                () -> authenticator.authenticate(request));
        assertEquals(SecurityResultCodeEnum.SESSION_MISSING, ex.getResultCode());
    }

}
