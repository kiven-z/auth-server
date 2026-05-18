package com.auth.service.auth.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.auth.common.core.model.response.Result;
import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.ChannelOptionsSupport;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.modules.email.EmailChannelOptions;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.audit.LoginAuditSnapshotFactory;
import com.auth.service.auth.authentication.mapper.UserMapper;
import com.auth.service.auth.authentication.service.impl.AuthMessageServiceImpl;
import com.auth.service.auth.support.integration.SystemSecurityIngestFeignClient;
import com.auth.service.auth.support.message.AuthEmailTemplateCode;
import com.auth.service.auth.support.message.AuthDypnsVerifyCodeSceneCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;
import static com.auth.module.security.contract.redis.SecurityRedisKey.SMS_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link AuthMessageServiceImpl} 单元测试
 */
@DisplayName("认证消息服务 - 登录邮箱验证码")
@ExtendWith(MockitoExtension.class)
class AuthMessageServiceImplTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private UserMapper userMapper;

	@Mock
	private IpAddressService ipAddressService;

	@Mock
	private SystemSecurityIngestFeignClient systemSecurityIngestFeignClient;

	private AuthMessageServiceImpl authMessageService;

	@BeforeEach
	void setUp() {
		LoginAuditSnapshotFactory snapshotFactory = new LoginAuditSnapshotFactory(ipAddressService);
		authMessageService = new AuthMessageServiceImpl(redisTemplate, userMapper, snapshotFactory,
				systemSecurityIngestFeignClient);
	}

	@Nested
	@DisplayName("正常流程：用户存在")
	class UserFound {

		@Test
		@DisplayName("写入 Redis MD5 并调用 system 邮件接口")
		void sendEmailCode_shouldStoreMd5AndInvokeEmailSend() {
			String email = "bunny@example.com";
			MockHttpServletRequest request = new MockHttpServletRequest();
			UserEntity user = new UserEntity();
			user.setUsername("Bunny");

			when(userMapper.selectUserByEmail(email)).thenReturn(user);
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(ipAddressService.resolveIpInfo(request))
				.thenReturn(IpInfo.builder().ipAddr("192.168.1.1").ipRegion("内网IP").build());
			when(systemSecurityIngestFeignClient.sendByTemplate(any(TemplateMessageCommand.class)))
				.thenReturn(Result.success());

			authMessageService.sendEmailCode(email, request);

			ArgumentCaptor<String> redisValueCaptor = ArgumentCaptor.forClass(String.class);
			verify(valueOperations).set(eq(EMAIL_CODE.key(email)), redisValueCaptor.capture(),
					eq(EMAIL_CODE.getDefaultTtl()));

			ArgumentCaptor<TemplateMessageCommand> commandCaptor = ArgumentCaptor
				.forClass(TemplateMessageCommand.class);
			verify(systemSecurityIngestFeignClient).sendByTemplate(commandCaptor.capture());

			TemplateMessageCommand command = commandCaptor.getValue();
			assertEquals(MessageChannel.EMAIL, command.getChannel());
			assertEquals(AuthEmailTemplateCode.LOGIN_EMAIL_CODE.getTemplateCode(), command.getTemplateCode());
			assertEquals(1, command.getTargets().size());
			assertEquals(email, command.getTargets().get(0));
			EmailChannelOptions emailOptions = ChannelOptionsSupport.asEmail(command.getOptions());
			assertEquals(Boolean.TRUE, emailOptions.getHasHtml());

			Object codeVar = command.getVariables().get("verificationCode");
			assertNotNull(codeVar);
			String code = codeVar.toString();
			assertEquals(6, code.length());
			assertEquals(MD5.create().digestHex16(code), redisValueCaptor.getValue());

			assertEquals("Bunny", command.getVariables().get("username"));
			assertEquals(EMAIL_CODE.getDefaultTtl().toMinutes(), command.getVariables().get("expireTime"));
			assertEquals("192.168.1.1", command.getVariables().get("ipAddr"));
			assertEquals("内网IP", command.getVariables().get("ipRegion"));
		}

	}

	@Nested
	@DisplayName("用户不存在：静默返回，不暴露账号状态")
	class UserNotFound {

		@Test
		@DisplayName("用户不存在时不发送邮件，不调用 Feign")
		void sendEmailCode_shouldSkipWhenUserNotFound() {
			String email = "unknown@example.com";
			MockHttpServletRequest request = new MockHttpServletRequest();

			when(userMapper.selectUserByEmail(email)).thenReturn(null);
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(ipAddressService.resolveIpInfo(request))
				.thenReturn(IpInfo.builder().ipAddr("192.168.1.1").ipRegion("内网IP").build());

			authMessageService.sendEmailCode(email, request);

			// 验证码仍正常写入 Redis（攻击者无法通过 NPE 或 4xx 判断邮箱是否存在）
			verify(valueOperations).set(eq(EMAIL_CODE.key(email)), any(), eq(EMAIL_CODE.getDefaultTtl()));

			// 不调用 Feign，不发邮件
			verify(systemSecurityIngestFeignClient, never()).sendByTemplate(any());
		}

	}

	@Nested
	@DisplayName("登录手机短信验证码")
	class SmsCode {

		@Test
		@DisplayName("用户存在：写入 Redis 并调用 Dypns 验证码短信模板发送")
		void sendSmsCode_shouldStoreMd5AndInvokeSmsSend() {
			String phone = "13800138000";
			UserEntity user = new UserEntity();
			user.setUsername("Bunny");

			when(userMapper.selectUserByPhone(phone)).thenReturn(user);
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(systemSecurityIngestFeignClient.sendByTemplate(any(TemplateMessageCommand.class)))
				.thenReturn(Result.success());

			authMessageService.sendSmsCode(phone, new MockHttpServletRequest());

			ArgumentCaptor<String> redisValueCaptor = ArgumentCaptor.forClass(String.class);
			verify(valueOperations).set(eq(SMS_CODE.key(phone)), redisValueCaptor.capture(),
					eq(SMS_CODE.getDefaultTtl()));

			ArgumentCaptor<TemplateMessageCommand> commandCaptor = ArgumentCaptor
				.forClass(TemplateMessageCommand.class);
			verify(systemSecurityIngestFeignClient).sendByTemplate(commandCaptor.capture());

			TemplateMessageCommand command = commandCaptor.getValue();
			assertEquals(MessageChannel.SMS_DYPNS_VERIFY_CODE, command.getChannel());
			assertEquals(AuthDypnsVerifyCodeSceneCode.LOGIN_VERIFY_CODE.getSceneCode(), command.getTemplateCode());
			assertEquals(phone, command.getTargets().get(0));
			Object codeVar = command.getVariables().get("code");
			assertNotNull(codeVar);
			String code = codeVar.toString();
			assertEquals(6, code.length());
			assertEquals(MD5.create().digestHex16(code), redisValueCaptor.getValue());
			assertEquals(SMS_CODE.getDefaultTtl().toMinutes(), command.getVariables().get("min"));
		}

		@Test
		@DisplayName("用户不存在：不调用 Feign")
		void sendSmsCode_shouldSkipWhenUserNotFound() {
			String phone = "13800138001";
			when(userMapper.selectUserByPhone(phone)).thenReturn(null);
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);

			authMessageService.sendSmsCode(phone, new MockHttpServletRequest());

			verify(valueOperations).set(eq(SMS_CODE.key(phone)), any(), eq(SMS_CODE.getDefaultTtl()));
			verify(systemSecurityIngestFeignClient, never()).sendByTemplate(any());
		}

	}

}
