package com.auth.module.security.core.token;

import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.facade.SecurityTokenHandlerFacade;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import com.auth.module.security.core.token.provider.RefreshTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityTokenHandlerFacadeTest {

    private static SecurityTokenResult newResult(SecurityTokenKind kind) {
        return SecurityTokenResult.builder().kind(kind).rawToken("raw").userToken(null).build();
    }

    @Test
    @DisplayName("测试验证内部令牌时应优先使用内部令牌")
    void validateFromHeaders_internalFirst() {
        StubAccessTokenProvider access = new StubAccessTokenProvider().withVerifyResult(true)
                .withParseResult(newResult(SecurityTokenKind.EXTERNAL_ACCESS));
        StubRefreshTokenProvider refresh = new StubRefreshTokenProvider().withVerifyResult(true)
                .withParseResult(newResult(SecurityTokenKind.EXTERNAL_REFRESH));
        StubInternalTokenProvider internal = new StubInternalTokenProvider().withVerifyResult()
                .withParseResult(newResult(SecurityTokenKind.INTERNAL));

        SecurityTokenHandlerFacade facade = new SecurityTokenHandlerFacade(access, refresh, internal);

        SecurityTokenResult result = facade.validateFromHeaders("Bearer external", "internal");
        assertNotNull(result);
        assertEquals(SecurityTokenKind.INTERNAL, result.getKind());

        assertEquals(1, internal.verifyCalls);
        assertEquals(1, internal.parseCalls);
        assertEquals(0, access.verifyCalls);
        assertEquals(0, refresh.verifyCalls);
    }

    @Test
    @DisplayName("测试验证外部令牌时应优先使用访问令牌")
    void validateExternal_bearerToken_usesAccessWhenValid() {
        StubAccessTokenProvider access = new StubAccessTokenProvider().withVerifyResult(true)
                .withParseResult(newResult(SecurityTokenKind.EXTERNAL_ACCESS));
        StubRefreshTokenProvider refresh = new StubRefreshTokenProvider().withVerifyResult(false);
        StubInternalTokenProvider internal = new StubInternalTokenProvider().withVerifyResult()
                .withParseResult(newResult(SecurityTokenKind.INTERNAL));

        SecurityTokenHandlerFacade facade = new SecurityTokenHandlerFacade(access, refresh, internal);

        SecurityTokenResult result = facade.validateFromHeaders("Bearer abc.def.ghi", null);
        assertNotNull(result);
        assertEquals(SecurityTokenKind.EXTERNAL_ACCESS, result.getKind());
        assertEquals(1, access.verifyCalls);
        assertEquals(1, access.parseCalls);
        assertEquals(0, refresh.parseCalls);
    }

    @Test
    @DisplayName("测试验证外部令牌时应回退到刷新令牌")
    void validateExternal_fallsBackToRefreshWhenAccessInvalid() {
        StubAccessTokenProvider access = new StubAccessTokenProvider().withVerifyResult(false);
        StubRefreshTokenProvider refresh = new StubRefreshTokenProvider().withVerifyResult(true)
                .withParseResult(newResult(SecurityTokenKind.EXTERNAL_REFRESH));
        StubInternalTokenProvider internal = new StubInternalTokenProvider().withVerifyResult()
                .withParseResult(newResult(SecurityTokenKind.INTERNAL));

        SecurityTokenHandlerFacade facade = new SecurityTokenHandlerFacade(access, refresh, internal);

        SecurityTokenResult result = facade.validateFromHeaders("Bearer refresh-token", null);
        assertNotNull(result);
        assertEquals(SecurityTokenKind.EXTERNAL_REFRESH, result.getKind());
        assertEquals(1, access.verifyCalls);
        assertEquals(1, refresh.verifyCalls);
        assertEquals(1, refresh.parseCalls);
    }

    @Test
    @DisplayName("测试验证令牌缺失时应抛出异常")
    void validateFromHeaders_missingTokens_throw() {
        StubAccessTokenProvider access = new StubAccessTokenProvider();
        StubRefreshTokenProvider refresh = new StubRefreshTokenProvider();
        StubInternalTokenProvider internal = new StubInternalTokenProvider();

        SecurityTokenHandlerFacade facade = new SecurityTokenHandlerFacade(access, refresh, internal);

        SecurityTokenException ex = assertThrows(SecurityTokenException.class,
                () -> facade.validateFromHeaders(null, null));
        assertEquals(SecurityResultCodeEnum.TOKEN_MISSING, ex.getResultCode());
    }

    // ---- Stub implementations ----
    // Each stub extends the concrete provider class but overrides all methods
    // that would otherwise delegate to the (null) JwtTokenProvider.

    private static final class StubAccessTokenProvider extends AccessTokenProvider {

        private boolean verifyResult;

        private SecurityTokenResult parseResult;

        private int verifyCalls;

        private int parseCalls;

        StubAccessTokenProvider() {
            super(null, null);
        }

        StubAccessTokenProvider withVerifyResult(boolean verifyResult) {
            this.verifyResult = verifyResult;
            return this;
        }

        StubAccessTokenProvider withParseResult(SecurityTokenResult parseResult) {
            this.parseResult = parseResult;
            return this;
        }

        @Override
        public boolean verifyToken(String token) {
            verifyCalls++;
            return verifyResult;
        }

        @Override
        public SecurityTokenResult parseToken(String token) {
            parseCalls++;
            return parseResult;
        }

        @Override
        public String buildToken(Long userId, String jti, Long permVersion) {
            throw new UnsupportedOperationException();
        }

    }

    private static final class StubRefreshTokenProvider extends RefreshTokenProvider {

        private boolean verifyResult;

        private SecurityTokenResult parseResult;

        private int verifyCalls;

        private int parseCalls;

        StubRefreshTokenProvider() {
            super(null, null);
        }

        StubRefreshTokenProvider withVerifyResult(boolean verifyResult) {
            this.verifyResult = verifyResult;
            return this;
        }

        StubRefreshTokenProvider withParseResult(SecurityTokenResult parseResult) {
            this.parseResult = parseResult;
            return this;
        }

        @Override
        public boolean verifyToken(String token) {
            verifyCalls++;
            return verifyResult;
        }

        @Override
        public SecurityTokenResult parseToken(String token) {
            parseCalls++;
            return parseResult;
        }

        @Override
        public String buildToken(Long userId, String jti, Long permVersion) {
            throw new UnsupportedOperationException();
        }

    }

    private static final class StubInternalTokenProvider extends InternalTokenProvider {

        private boolean verifyResult;

        private SecurityTokenResult parseResult;

        private int verifyCalls;

        private int parseCalls;

        StubInternalTokenProvider() {
            super(null, null);
        }

        StubInternalTokenProvider withVerifyResult() {
            this.verifyResult = true;
            return this;
        }

        StubInternalTokenProvider withParseResult(SecurityTokenResult parseResult) {
            this.parseResult = parseResult;
            return this;
        }

        @Override
        public boolean verifyToken(String token) {
            verifyCalls++;
            return verifyResult;
        }

        @Override
        public SecurityTokenResult parseToken(String token) {
            parseCalls++;
            return parseResult;
        }

        @Override
        public String buildToken(Long userId, String jti) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String buildServiceToken(String serviceId, String jti) {
            throw new UnsupportedOperationException();
        }

    }

}
