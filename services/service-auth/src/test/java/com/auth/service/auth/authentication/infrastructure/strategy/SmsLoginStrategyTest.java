package com.auth.service.auth.authentication.infrastructure.strategy;

import cn.hutool.crypto.digest.MD5;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.api.dto.request.SmsLoginRequest;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.authentication.infrastructure.token.LoginFailureRateLimiter;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SmsLoginStrategy} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class SmsLoginStrategyTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private LoginFailureRateLimiter loginFailureRateLimiter;

    @Mock
    private AuthProfileQueryService authProfileQueryService;

    @Mock
    private LoginAuditEventPublisher loginAuditEventPublisher;

    @Mock
    private HttpServletRequest httpServletRequest;

    private SmsLoginStrategy createStrategy() {
        return new SmsLoginStrategy(authProfileQueryService, redisTemplate, userMapper, loginFailureRateLimiter,
                loginAuditEventPublisher);
    }

    private SmsLoginStrategy createStrategyWithRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return createStrategy();
    }

    @Test
    @DisplayName("手机号未注册时抛出 USERNAME_OR_PASSWORD_ERROR 并发布审计事件")
    void authenticate_shouldThrow_whenPhoneNotRegistered() {
        // Arrange
        when(userMapper.selectUserForLoginByPhone("13800138000")).thenReturn(null);
        SmsLoginStrategy strategy = createStrategy();

        SmsLoginRequest auth = new SmsLoginRequest();
        auth.setPhone("13800138000");
        auth.setCode("123456");

        // Act
        AuthBusinessException exception = assertThrows(AuthBusinessException.class,
                () -> strategy.authenticate(auth, httpServletRequest));

        // Assert
        assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR, exception.getResultCode());
        verify(loginAuditEventPublisher).publish(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_SMS),
                eq(AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR), isNull(), eq("13800138000"),
                eq(exception.getMessageKey()), isNull());
    }

    @Test
    @DisplayName("验证码错误时抛出 AUTH_CODE_ERROR")
    void doAuthenticate_shouldThrow_whenCodeMismatch() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPhone("13800138000");
        user.setStatus(1);
        when(userMapper.selectUserForLoginByPhone("13800138000")).thenReturn(user);
        doNothing().when(loginFailureRateLimiter).assertNotLocked(1L);
        SmsLoginStrategy strategy = createStrategyWithRedis();

        String phone = "13800138000";
        String redisKey = "auth:security:login:sms:code:" + phone;
        when(valueOperations.get(redisKey)).thenReturn(MD5.create().digestHex16("wrong"));

        SmsLoginRequest auth = new SmsLoginRequest();
        auth.setPhone("13800138000");
        auth.setCode("000000");

        // Act
        AuthBusinessException exception = assertThrows(AuthBusinessException.class,
                () -> strategy.authenticate(auth, httpServletRequest));

        // Assert
        assertEquals(AuthResultCode.AUTH_CODE_ERROR, exception.getResultCode());
        verify(loginAuditEventPublisher).publish(same(httpServletRequest), eq(AuthLoginLogType.LOGIN_SMS),
                eq(AuthLoginLogResult.CAPTCHA_ERROR), eq(1L), eq("testUser"), eq(exception.getMessageKey()), isNull());
    }

    @Test
    @DisplayName("验证码正确时登录成功并删除 Redis 中的验证码")
    void doAuthenticate_shouldSucceed_whenCodeMatches() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testUser");
        user.setPhone("13800138000");
        user.setNickname("Test");
        user.setStatus(1);
        when(userMapper.selectUserForLoginByPhone("13800138000")).thenReturn(user);
        doNothing().when(loginFailureRateLimiter).assertNotLocked(1L);

        AuthProfile profile = mock(AuthProfile.class);
        when(profile.getRoles()).thenReturn(List.of());
        when(profile.getPermissions()).thenReturn(List.of());
        when(authProfileQueryService.buildByUser(user)).thenReturn(profile);
        SmsLoginStrategy strategy = createStrategyWithRedis();

        String phone = "13800138000";
        String code = "123456";
        String redisKey = "auth:security:login:sms:code:" + phone;
        when(valueOperations.get(redisKey)).thenReturn(MD5.create().digestHex16(code));

        SmsLoginRequest auth = new SmsLoginRequest();
        auth.setPhone(phone);
        auth.setCode(code);

        // Act
        strategy.authenticate(auth, httpServletRequest);

        // Assert
        verify(redisTemplate).delete(redisKey);
    }

    @Test
    @DisplayName("supports 返回 SmsLoginRequest.class")
    void supports_shouldReturnSmsLoginRequestClass() {
        SmsLoginStrategy strategy = createStrategy();
        assertEquals(SmsLoginRequest.class, strategy.supports());
    }

}
