package com.auth.service.auth.authentication.application;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.ChannelOptionsSupport;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import com.auth.service.auth.authentication.domain.port.LoginAccountLookupPort;
import com.auth.service.auth.authentication.infrastructure.audit.LoginAuditSnapshotFactory;
import com.auth.service.auth.authentication.infrastructure.message.AuthDypnsVerifyCodeSceneCode;
import com.auth.service.auth.authentication.infrastructure.message.AuthEmailTemplateCode;
import com.auth.service.auth.shared.integration.SystemSecurityIngestFeignClient;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.Optional;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;
import static com.auth.module.security.contract.redis.SecurityRedisKey.SMS_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthMessageService} 单元测试
 */
@DisplayName("AuthMessageService 认证消息服务")
@ExtendWith(MockitoExtension.class)
class AuthMessageServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private LoginAccountLookupPort loginAccountLookupPort;

	@Mock
	private LoginAuditSnapshotFactory loginAuditSnapshotFactory;

	@Mock
	private SystemSecurityIngestFeignClient systemSecurityIngestFeignClient;

	@Mock
	private HttpServletRequest request;

	private AuthMessageService authMessageService;

	@BeforeEach
	void setUp() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		authMessageService = new AuthMessageService(redisTemplate, loginAccountLookupPort, loginAuditSnapshotFactory,
				systemSecurityIngestFeignClient);
	}

	@Nested
	@DisplayName("sendEmailCode")
	class SendEmailCode {

		@Test
		@DisplayName("用户存在时写入 Redis 并 Feign 委托 system 发信")
		void sendEmailCode_shouldStoreMd5AndInvokeEmailSend() {
			String email = "bunny@example.com";
			LoginAccount account = new LoginAccount(1L, "bunny", 1, null, null, null, 1L);
			when(loginAccountLookupPort.findActiveOnly(CredentialDimension.EMAIL, email))
				.thenReturn(Optional.of(account));
			when(loginAuditSnapshotFactory.build(request))
				.thenReturn(new LoginAuditSnapshot("127.0.0.1", "本地", "ua", "pc", "win", "chrome"));
			when(systemSecurityIngestFeignClient.sendByTemplate(any(TemplateMessageCommand.class)))
				.thenReturn(Result.success("ok"));

			authMessageService.sendEmailCode(email, request);

			verify(valueOperations).set(eq(EMAIL_CODE.key(email)), any(), eq(EMAIL_CODE.getDefaultTtl()));
			ArgumentCaptor<TemplateMessageCommand> commandCaptor = ArgumentCaptor
				.forClass(TemplateMessageCommand.class);
			verify(systemSecurityIngestFeignClient).sendByTemplate(commandCaptor.capture());
			TemplateMessageCommand command = commandCaptor.getValue();
			assertEquals(MessageChannel.EMAIL, command.getChannel());
			assertEquals(AuthEmailTemplateCode.LOGIN_EMAIL_CODE.getTemplateCode(), command.getTemplateCode());
			assertEquals(email, command.getTargets().get(0));
			EmailChannelOptions emailOptions = ChannelOptionsSupport.asEmail(command.getOptions());
			assertEquals(Boolean.TRUE, emailOptions.getHasHtml());
		}

		@Test
		@DisplayName("用户不存在时静默返回且不调用 Feign")
		void sendEmailCode_shouldSkipWhenUserNotFound() {
			String email = "unknown@example.com";
			when(loginAccountLookupPort.findActiveOnly(CredentialDimension.EMAIL, email)).thenReturn(Optional.empty());

			authMessageService.sendEmailCode(email, request);

			verify(valueOperations).set(eq(EMAIL_CODE.key(email)), any(), eq(EMAIL_CODE.getDefaultTtl()));
			verify(systemSecurityIngestFeignClient, never()).sendByTemplate(any());
		}

	}

	@Nested
	@DisplayName("sendSmsCode")
	class SendSmsCode {

		@Test
		@DisplayName("用户存在时写入 Redis 并 Feign 委托 system 发短信")
		void sendSmsCode_shouldStoreMd5AndInvokeSmsSend() {
			String phone = "13800000000";
			LoginAccount account = new LoginAccount(1L, "bunny", 1, null, null, null, 1L);
			when(loginAccountLookupPort.findActiveOnly(CredentialDimension.PHONE, phone))
				.thenReturn(Optional.of(account));
			when(systemSecurityIngestFeignClient.sendByTemplate(any(TemplateMessageCommand.class)))
				.thenReturn(Result.success("ok"));

			authMessageService.sendSmsCode(phone);

			verify(valueOperations).set(eq(SMS_CODE.key(phone)), any(), eq(SMS_CODE.getDefaultTtl()));
			ArgumentCaptor<TemplateMessageCommand> commandCaptor = ArgumentCaptor
				.forClass(TemplateMessageCommand.class);
			verify(systemSecurityIngestFeignClient).sendByTemplate(commandCaptor.capture());
			TemplateMessageCommand command = commandCaptor.getValue();
			assertEquals(MessageChannel.SMS_ALIYUN_DYPNS, command.getChannel());
			assertEquals(AuthDypnsVerifyCodeSceneCode.LOGIN_VERIFY_CODE.getSceneCode(), command.getTemplateCode());
			assertEquals(phone, command.getTargets().get(0));
		}

		@Test
		@DisplayName("用户不存在时静默返回且不调用 Feign")
		void sendSmsCode_shouldSkipWhenUserNotFound() {
			String phone = "13800000001";
			when(loginAccountLookupPort.findActiveOnly(CredentialDimension.PHONE, phone)).thenReturn(Optional.empty());

			authMessageService.sendSmsCode(phone);

			verify(valueOperations).set(eq(SMS_CODE.key(phone)), any(), eq(SMS_CODE.getDefaultTtl()));
			verify(systemSecurityIngestFeignClient, never()).sendByTemplate(any());
		}

	}

}
