package com.auth.service.auth.application.login;

import com.auth.common.core.model.response.Result;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.auth.application.login.message.AuthMessageScene;
import com.auth.service.auth.application.port.login.LoginAuditSnapshotPort;
import com.auth.service.auth.application.port.CredentialGateway;
import com.auth.service.auth.domain.authentication.enums.CredentialDimension;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.domain.authentication.model.LoginAuditSnapshot;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link AuthMessageService} 单元测试
 */
@DisplayName("AuthMessageService 认证消息服务")
@ExtendWith(MockitoExtension.class)
class AuthMessageServiceTest {

	@Mock
	private CredentialGateway credentialGateway;

	@Mock
	private LoginAuditSnapshotPort loginAuditSnapshotPort;

	@Mock
	private SystemSecurityIngestFeignClient systemSecurityIngestFeignClient;

	@Mock
	private HttpServletRequest request;

	private AuthMessageService authMessageService;

	@BeforeEach
	void setUp() {
		authMessageService = new AuthMessageService(credentialGateway, loginAuditSnapshotPort,
				systemSecurityIngestFeignClient);
	}

	@Nested
	@DisplayName("sendEmailCode")
	class SendEmailCode {

		@Test
		@DisplayName("用户存在时写入验证码并 Feign 委托 system 发信")
		void sendEmailCode_shouldStoreMd5AndInvokeEmailSend() {
			String email = "bunny@example.com";
			LoginAccount account = new LoginAccount(1L, "bunny", 1, null, null, null, 1L);
			when(credentialGateway.findActiveOnly(CredentialDimension.EMAIL, email)).thenReturn(Optional.of(account));
			when(loginAuditSnapshotPort.build(request))
				.thenReturn(new LoginAuditSnapshot("127.0.0.1", "本地", "ua", "pc", "win", "chrome"));
			when(systemSecurityIngestFeignClient.sendByTemplate(any(TemplateMessageCommand.class)))
				.thenReturn(Result.success("ok"));

			authMessageService.sendEmailCode(email, request);

			verify(credentialGateway).storeDigest(eq(CredentialDimension.EMAIL), eq(email), any());
			ArgumentCaptor<TemplateMessageCommand> commandCaptor = ArgumentCaptor
				.forClass(TemplateMessageCommand.class);
			verify(systemSecurityIngestFeignClient).sendByTemplate(commandCaptor.capture());
			TemplateMessageCommand command = commandCaptor.getValue();
			assertEquals(MessageChannel.EMAIL, command.getChannel());
			assertEquals(AuthMessageScene.LOGIN_EMAIL.getTemplateCode(), command.getTemplateCode());
			assertEquals(email, command.getTargets().get(0));
			EmailChannelOptions emailOptions = (EmailChannelOptions) command.getOptions();
			assertEquals(Boolean.TRUE, emailOptions.getHasHtml());
		}

		@Test
		@DisplayName("用户不存在时静默返回且不调用 Feign")
		void sendEmailCode_shouldSkipWhenUserNotFound() {
			String email = "unknown@example.com";
			when(credentialGateway.findActiveOnly(CredentialDimension.EMAIL, email)).thenReturn(Optional.empty());

			authMessageService.sendEmailCode(email, request);

			verify(credentialGateway).storeDigest(eq(CredentialDimension.EMAIL), eq(email), any());
			verify(systemSecurityIngestFeignClient, never()).sendByTemplate(any());
		}

	}

	@Nested
	@DisplayName("sendSmsCode")
	class SendSmsCode {

		@Test
		@DisplayName("用户存在时写入验证码并 Feign 委托 system 发短信")
		void sendSmsCode_shouldStoreMd5AndInvokeSmsSend() {
			String phone = "13800000000";
			LoginAccount account = new LoginAccount(1L, "bunny", 1, null, null, null, 1L);
			when(credentialGateway.findActiveOnly(CredentialDimension.PHONE, phone)).thenReturn(Optional.of(account));
			when(systemSecurityIngestFeignClient.sendByTemplate(any(TemplateMessageCommand.class)))
				.thenReturn(Result.success("ok"));

			authMessageService.sendSmsCode(phone);

			verify(credentialGateway).storeDigest(eq(CredentialDimension.PHONE), eq(phone), any());
			ArgumentCaptor<TemplateMessageCommand> commandCaptor = ArgumentCaptor
				.forClass(TemplateMessageCommand.class);
			verify(systemSecurityIngestFeignClient).sendByTemplate(commandCaptor.capture());
			TemplateMessageCommand command = commandCaptor.getValue();
			assertEquals(MessageChannel.SMS_ALIYUN_DYPNS, command.getChannel());
			assertEquals(AuthMessageScene.LOGIN_SMS.getTemplateCode(), command.getTemplateCode());
			assertEquals(phone, command.getTargets().get(0));
		}

		@Test
		@DisplayName("用户不存在时静默返回且不调用 Feign")
		void sendSmsCode_shouldSkipWhenUserNotFound() {
			String phone = "13800000001";
			when(credentialGateway.findActiveOnly(CredentialDimension.PHONE, phone)).thenReturn(Optional.empty());

			authMessageService.sendSmsCode(phone);

			verify(credentialGateway).storeDigest(eq(CredentialDimension.PHONE), eq(phone), any());
			verify(systemSecurityIngestFeignClient, never()).sendByTemplate(any());
		}

	}

}
