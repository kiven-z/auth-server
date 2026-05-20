package com.auth.service.auth.shared.ratelimit;

import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * RateLimitAspect 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private DummyService proxyWithAspect() {
        RateLimitAspect aspect = new RateLimitAspect(redisTemplate);
        AspectJProxyFactory factory = new AspectJProxyFactory(new DummyService());
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    @Test
    @DisplayName("限流：principal 为空时抛 BAD_REQUEST_MISSING_EMAIL_EXCEPTION")
    void checkRateLimit_shouldThrowMissingEmail_whenPrincipalBlank() {
        // Arrange
        DummyService proxy = proxyWithAspect();

        // Act
        AuthBusinessException exception = assertThrows(AuthBusinessException.class, proxy::missingPrincipal);

        // Assert
        assertEquals(AuthResultCode.BAD_REQUEST_MISSING_EMAIL_EXCEPTION, exception.getResultCode());
    }

    @Test
    @DisplayName("限流：计数超过上限时抛 TOO_MANY_REQUESTS")
    void checkRateLimit_shouldThrowTooManyRequests_whenCountExceeded() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(6L);
        DummyService proxy = proxyWithAspect();

        // Act
        AuthBusinessException exception = assertThrows(AuthBusinessException.class, proxy::sendCode);

        // Assert
        assertEquals(AuthResultCode.TOO_MANY_REQUESTS, exception.getResultCode());
    }

    @Test
    @DisplayName("限流：首次计数时 expire 返回 false，抛 TOO_MANY_REQUESTS")
    void checkRateLimit_shouldThrowTooManyRequests_whenExpireFailedOnFirstCount() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), any())).thenReturn(false);
        DummyService proxy = proxyWithAspect();

        // Act
        AuthBusinessException exception = assertThrows(AuthBusinessException.class, proxy::sendCode);

        // Assert
        assertEquals(AuthResultCode.TOO_MANY_REQUESTS, exception.getResultCode());
    }

    static class DummyService {

        @RateLimit()
        void missingPrincipal() {
        }

        @RateLimit(principal = "user@example.com")
        void sendCode() {
        }

    }

}
