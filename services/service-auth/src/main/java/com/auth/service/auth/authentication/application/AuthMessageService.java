package com.auth.service.auth.authentication.application;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.service.auth.authentication.application.message.AuthMessageScene;
import com.auth.service.auth.authentication.application.message.AuthMessageVariables;
import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import com.auth.service.auth.authentication.application.gateway.CredentialGateway;
import com.auth.service.auth.authentication.domain.port.LoginAuditSnapshotPort;
import com.auth.service.auth.shared.integration.SystemSecurityIngestFeignClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;
import static com.auth.module.security.contract.redis.SecurityRedisKey.SMS_CODE;

/**
 * 认证消息应用服务：登录邮箱/短信验证码发送。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthMessageService {

	private final CredentialGateway credentialGateway;

	private final LoginAuditSnapshotPort loginAuditSnapshotPort;

	private final SystemSecurityIngestFeignClient systemSecurityIngestFeignClient;

	/**
	 * 发送登录邮箱验证码
	 * @param email 邮箱
	 * @param request 当前 HTTP 请求（用于解析登录 IP 等信息）
	 */
	public void sendEmailCode(String email, HttpServletRequest request) {
		String code = RandomUtil.randomNumbers(6);
		String digest = MD5.create().digestHex16(code);

		// 存入 Redis 时使用 MD5，登录校验侧对比 digestHex16
		credentialGateway.storeDigest(CredentialDimension.EMAIL, email, digest);

		// 过期时间转成分钟
		long expireMinutes = EMAIL_CODE.getDefaultTtl().toMinutes();

		// IP地址等信息
		LoginAuditSnapshot snapshot = loginAuditSnapshotPort.build(request);

		// 未找到活跃用户时静默返回，不暴露账号不存在
		LoginAccount account = credentialGateway.findActiveOnly(CredentialDimension.EMAIL, email).orElse(null);
		if (account == null) {
			log.warn("Send email code skipped: no active user found for email={}", email);
			return;
		}

		// 拼装发送对象
		String username = account.username();
		Map<String, Object> variables = AuthMessageVariables.buildLoginEmail(username, code, expireMinutes,
				LocalDateTime.now(), snapshot.loginIp(), snapshot.loginRegion());
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(AuthMessageScene.LOGIN_EMAIL.getChannel())
			.templateCode(AuthMessageScene.LOGIN_EMAIL.getTemplateCode())
			.targets(List.of(email))
			.variables(variables)
			.options(EmailChannelOptions.builder().hasHtml(Boolean.TRUE).build())
			.build();

		systemSecurityIngestFeignClient.sendByTemplate(command);
	}

	/**
	 * 发送登录手机短信验证码
	 * @param phone 手机号
	 */
	public void sendSmsCode(String phone) {
		String code = RandomUtil.randomNumbers(6);
		String digest = MD5.create().digestHex16(code);

		// 存入Redis
		credentialGateway.storeDigest(CredentialDimension.PHONE, phone, digest);

		// 存入Redis
		long expireMinutes = SMS_CODE.getDefaultTtl().toMinutes();

		if (credentialGateway.findActiveOnly(CredentialDimension.PHONE, phone).isEmpty()) {
			log.warn("Send SMS code skipped: no active user found for phone={}", phone);
			return;
		}

		// 拼装发送对象
		Map<String, Object> variables = AuthMessageVariables.buildLoginSms(code, expireMinutes);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(AuthMessageScene.LOGIN_SMS.getChannel())
			.templateCode(AuthMessageScene.LOGIN_SMS.getTemplateCode())
			.targets(List.of(phone))
			.variables(variables)
			.build();
		systemSecurityIngestFeignClient.sendByTemplate(command);
	}

}
