package com.auth.service.auth.authentication.infrastructure.strategy;

import cn.hutool.crypto.digest.MD5;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.application.command.SmsLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialField;
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
	private LoginAuditCoordinator loginAuditCoordinator;

	@Mock
	private HttpServletRequest httpServletRequest;

	private SmsLoginStrategy createStrategy() {
		return new SmsLoginStrategy(authProfileQueryService, redisTemplate, userMapper, loginFailureRateLimiter,
				loginAuditCoordinator);
	}

	private SmsLoginStrategy createStrategyWithRedis() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		return createStrategy();
	}

	@Test
	@DisplayName("手机号未注册时抛出 USERNAME_OR_PASSWORD_ERROR 并发布审计事件")
	void authenticate_shouldThrow_whenPhoneNotRegistered() {
		when(userMapper.selectByCredential(UserCredentialField.PHONE, "13800138000", false)).thenReturn(null);
		SmsLoginStrategy strategy = createStrategy();

		SmsLoginCommand command = new SmsLoginCommand("13800138000", "123456");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR, exception.getResultCode());
		verify(loginAuditCoordinator).auditUserUnknownAfterResolve(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_SMS), eq("13800138000"), eq(exception.getMessageKey()));
	}

	@Test
	@DisplayName("验证码错误时抛出 AUTH_CODE_ERROR")
	void doAuthenticate_shouldThrow_whenCodeMismatch() {
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setUsername("testUser");
		user.setPhone("13800138000");
		user.setStatus(1);
		when(userMapper.selectByCredential(UserCredentialField.PHONE, "13800138000", false)).thenReturn(user);
		doNothing().when(loginFailureRateLimiter).assertNotLocked(1L);
		SmsLoginStrategy strategy = createStrategyWithRedis();

		String phone = "13800138000";
		String redisKey = "auth:security:login:sms:code:" + phone;
		when(valueOperations.get(redisKey)).thenReturn(MD5.create().digestHex16("wrong"));

		SmsLoginCommand command = new SmsLoginCommand("13800138000", "000000");

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> strategy.authenticate(command, httpServletRequest));

		assertEquals(AuthResultCode.AUTH_CODE_ERROR, exception.getResultCode());
		verify(loginAuditCoordinator).auditMappedCredentialFailure(same(httpServletRequest),
				eq(AuthLoginLogType.LOGIN_SMS), eq(1L), eq("testUser"), eq(exception));
	}

	@Test
	@DisplayName("验证码正确时登录成功并删除 Redis 中的验证码")
	void doAuthenticate_shouldSucceed_whenCodeMatches() {
		UserEntity user = new UserEntity();
		user.setId(1L);
		user.setUsername("testUser");
		user.setPhone("13800138000");
		user.setNickname("Test");
		user.setStatus(1);
		when(userMapper.selectByCredential(UserCredentialField.PHONE, "13800138000", false)).thenReturn(user);
		doNothing().when(loginFailureRateLimiter).assertNotLocked(1L);

		AuthProfile profile = mock(AuthProfile.class);
		when(authProfileQueryService.buildByUser(user)).thenReturn(profile);
		SmsLoginStrategy strategy = createStrategyWithRedis();

		String phone = "13800138000";
		String code = "123456";
		String redisKey = "auth:security:login:sms:code:" + phone;
		when(valueOperations.get(redisKey)).thenReturn(MD5.create().digestHex16(code));

		SmsLoginCommand command = new SmsLoginCommand(phone, code);

		strategy.authenticate(command, httpServletRequest);

		verify(redisTemplate).delete(redisKey);
	}

	@Test
	@DisplayName("supports 返回 SmsLoginCommand.class")
	void supports_shouldReturnSmsLoginCommandClass() {
		SmsLoginStrategy strategy = createStrategy();
		assertEquals(SmsLoginCommand.class, strategy.supports());
	}

}
