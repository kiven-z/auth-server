package com.auth.service.auth.authentication.application;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.module.message.api.channel.MessageChannel;
import com.auth.module.message.api.command.TemplateMessageCommand;
import com.auth.module.message.api.model.email.EmailChannelOptions;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import com.auth.service.auth.authentication.infrastructure.audit.LoginAuditSnapshotFactory;
import com.auth.service.auth.authentication.infrastructure.message.AuthDypnsVerifyCodeSceneCode;
import com.auth.service.auth.authentication.infrastructure.message.AuthEmailTemplateCode;
import com.auth.service.auth.authentication.infrastructure.message.LoginEmailVariables;
import com.auth.service.auth.authentication.infrastructure.message.LoginSmsVariables;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialField;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialQueries;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import com.auth.service.auth.shared.integration.SystemSecurityIngestFeignClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
@Slf4j
@Service
public class AuthMessageService {

	private final RedisTemplate<String, Object> redisTemplate;

	private final UserMapper userMapper;

	private final LoginAuditSnapshotFactory loginAuditSnapshotFactory;

	private final SystemSecurityIngestFeignClient systemSecurityIngestFeignClient;

	public AuthMessageService(RedisTemplate<String, Object> redisTemplate, UserMapper userMapper,
			LoginAuditSnapshotFactory loginAuditSnapshotFactory,
			SystemSecurityIngestFeignClient systemSecurityIngestFeignClient) {
		this.redisTemplate = redisTemplate;
		this.userMapper = userMapper;
		this.loginAuditSnapshotFactory = loginAuditSnapshotFactory;
		this.systemSecurityIngestFeignClient = systemSecurityIngestFeignClient;
	}

	/**
	 * 发送登录邮箱验证码
	 * @param email 邮箱
	 * @param request 当前 HTTP 请求（用于解析登录 IP 等信息）
	 */
	public void sendEmailCode(String email, HttpServletRequest request) {
		String redisKey = EMAIL_CODE.key(email);
		String code = RandomUtil.randomNumbers(6);

		// 存入 Redis 时使用 MD5，登录校验侧对比 digestHex16
		redisTemplate.opsForValue().set(redisKey, MD5.create().digestHex16(code), EMAIL_CODE.getDefaultTtl());

		// 过期时间转成分钟
		long expireMinutes = EMAIL_CODE.getDefaultTtl().toMinutes();

		// IP地址等信息
		LoginAuditSnapshot snapshot = loginAuditSnapshotFactory.build(request);

		// 未找到活跃用户时静默返回，不暴露账号不存在（与 EmailLoginStrategy 策略一致）
		UserEntity user = UserCredentialQueries.activeOnly(userMapper, UserCredentialField.EMAIL, email);
		if (user == null) {
			log.warn("Send email code skipped: no active user found for email={}", email);
			return;
		}
		String username = user.getUsername();

		// 拼装发送对象
		Map<String, Object> variables = LoginEmailVariables.build(username, code, expireMinutes, LocalDateTime.now(),
				snapshot.loginIp(), snapshot.loginRegion());
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode(AuthEmailTemplateCode.LOGIN_EMAIL_CODE.getTemplateCode())
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
		String redisKey = SMS_CODE.key(phone);
		String code = RandomUtil.randomNumbers(6);

		// 存入Redis
		redisTemplate.opsForValue().set(redisKey, MD5.create().digestHex16(code), SMS_CODE.getDefaultTtl());

		// 获取过期时间
		long expireMinutes = SMS_CODE.getDefaultTtl().toMinutes();

		UserEntity user = UserCredentialQueries.activeOnly(userMapper, UserCredentialField.PHONE, phone);
		if (user == null) {
			log.warn("Send SMS code skipped: no active user found for phone={}", phone);
			return;
		}

		// 构建发送内容
		Map<String, Object> variables = LoginSmsVariables.build(code, expireMinutes);
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.SMS_ALIYUN_DYPNS)
			.templateCode(AuthDypnsVerifyCodeSceneCode.LOGIN_VERIFY_CODE.getSceneCode())
			.targets(List.of(phone))
			.variables(variables)
			.build();

		systemSecurityIngestFeignClient.sendByTemplate(command);
	}

}
