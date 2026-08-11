package com.auth.service.auth.service;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.feign.SystemMessageSendFeignClient;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.module.message.api.model.enums.scene.EmailMessageScene;
import com.auth.module.message.api.model.enums.scene.SmsMessageScene;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.CredentialDimension;
import com.auth.service.auth.model.value.login.LoginAuditSnapshot;
import com.auth.service.auth.service.impl.AuthMessageServiceImpl;
import com.auth.service.auth.support.login.LoginLogRepository;
import com.auth.service.auth.support.redis.store.LoginVerificationCodeStore;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link AuthMessageService} 单元测试
 */
@DisplayName("AuthMessageApplicationService 认证消息服务")
@ExtendWith(MockitoExtension.class)
class AuthMessageServiceTest {

	@Mock
	private LoginLogRepository loginLogRepository;

	@Mock
	private SystemMessageSendFeignClient systemMessageSendFeignClient;

	@Mock
	private LoginVerificationCodeStore loginVerificationCodeStore;

	@Mock
	private UserMapper userMapper;

	@Mock
	private HttpServletRequest request;

	private AuthMessageService authMessageService;

	@BeforeEach
	void setUp() {
		authMessageService = new AuthMessageServiceImpl(loginLogRepository, systemMessageSendFeignClient, userMapper,
				loginVerificationCodeStore);
	}

	@Nested
	@DisplayName("sendEmailCode")
	class SendEmailCode {

		@Test
		@DisplayName("用户存在时写入验证码并 Feign 委托 system 发信")
		void sendEmailCode_shouldStoreMd5AndInvokeEmailSend() {
			String email = "bunny@example.com";
			UserEntity userEntity = new UserEntity();
			userEntity.setId(1L);
			userEntity.setUsername("bunny");
			userEntity.setStatus(1);
			userEntity.setPermVersion(1L);
			when(userMapper.selectByCredential(CredentialDimension.EMAIL, email, true)).thenReturn(userEntity);
			when(loginLogRepository.buildSnapshot(request)).thenReturn(LoginAuditSnapshot.builder()
				.loginIp("127.0.0.1")
				.loginRegion("本地")
				.userAgent("ua")
				.deviceType("pc")
				.osType("win")
				.browserType("chrome")
				.build());
			when(systemMessageSendFeignClient.sendByTemplate(any(TemplateMessageCommand.class)))
				.thenReturn(Result.success());

			authMessageService.sendEmailCode(email, request);

			verify(loginVerificationCodeStore).storeDigest(eq(CredentialDimension.EMAIL), eq(email), any());
			ArgumentCaptor<TemplateMessageCommand> commandCaptor = ArgumentCaptor
				.forClass(TemplateMessageCommand.class);
			verify(systemMessageSendFeignClient).sendByTemplate(commandCaptor.capture());
			TemplateMessageCommand command = commandCaptor.getValue();
			assertEquals(MessageChannel.EMAIL, command.getChannel());
			assertEquals(EmailMessageScene.LOGIN_EMAIL.getTemplateCode(), command.getTemplateCode());
			assertEquals(email, command.getTargets().get(0));
			EmailChannelOptions emailOptions = (EmailChannelOptions) command.getOptions();
			assertEquals(Boolean.TRUE, emailOptions.getHasHtml());
		}

		@Test
		@DisplayName("用户不存在时静默返回且不调用 Feign")
		void sendEmailCode_shouldSkipWhenUserNotFound() {
			String email = "unknown@example.com";
			when(userMapper.selectByCredential(CredentialDimension.EMAIL, email, true)).thenReturn(null);

			authMessageService.sendEmailCode(email, request);

			verify(loginVerificationCodeStore).storeDigest(eq(CredentialDimension.EMAIL), eq(email), any());
			verify(systemMessageSendFeignClient, never()).sendByTemplate(any());
		}

	}

	@Nested
	@DisplayName("sendSmsCode")
	class SendSmsCode {

		@Test
		@DisplayName("用户存在时写入验证码并 Feign 委托 system 发短信")
		void sendSmsCode_shouldStoreMd5AndInvokeSmsSend() {
			String phone = "13800000000";
			UserEntity userEntity = new UserEntity();
			userEntity.setId(1L);
			userEntity.setUsername("bunny");
			userEntity.setStatus(1);
			userEntity.setPermVersion(1L);
			when(userMapper.selectByCredential(CredentialDimension.PHONE, phone, true)).thenReturn(userEntity);
			when(systemMessageSendFeignClient.sendByTemplate(any(TemplateMessageCommand.class)))
				.thenReturn(Result.success());

			authMessageService.sendSmsCode(phone);

			verify(loginVerificationCodeStore).storeDigest(eq(CredentialDimension.PHONE), eq(phone), any());
			ArgumentCaptor<TemplateMessageCommand> commandCaptor = ArgumentCaptor
				.forClass(TemplateMessageCommand.class);
			verify(systemMessageSendFeignClient).sendByTemplate(commandCaptor.capture());
			TemplateMessageCommand command = commandCaptor.getValue();
			assertEquals(MessageChannel.SMS, command.getChannel());
			assertEquals(SmsMessageScene.LOGIN_SMS.getTemplateCode(), command.getTemplateCode());
			assertEquals(phone, command.getTargets().get(0));
		}

		@Test
		@DisplayName("用户不存在时静默返回且不调用 Feign")
		void sendSmsCode_shouldSkipWhenUserNotFound() {
			String phone = "13800000001";
			when(userMapper.selectByCredential(CredentialDimension.PHONE, phone, true)).thenReturn(null);

			authMessageService.sendSmsCode(phone);

			verify(loginVerificationCodeStore).storeDigest(eq(CredentialDimension.PHONE), eq(phone), any());
			verify(systemMessageSendFeignClient, never()).sendByTemplate(any());
		}

	}

}
