package com.auth.service.auth.authentication.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.module.message.channel.MessageChannel;
import com.auth.module.message.command.TemplateMessageCommand;
import com.auth.module.message.email.EmailChannelOptions;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.authentication.audit.LoginAuditSnapshot;
import com.auth.service.auth.authentication.audit.LoginAuditSnapshotFactory;
import com.auth.service.auth.authentication.mapper.UserMapper;
import com.auth.service.auth.authentication.service.AuthMessageService;
import com.auth.service.auth.support.integration.SystemMessageSendFeignClient;
import com.auth.service.auth.support.message.AuthEmailTemplateCode;
import com.auth.service.auth.support.message.LoginEmailVariables;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;

/**
 * 认证消息服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
public class AuthMessageServiceImpl implements AuthMessageService {

	private final RedisTemplate<String, Object> redisTemplate;

	private final UserMapper userMapper;

	private final LoginAuditSnapshotFactory loginAuditSnapshotFactory;

	private final SystemMessageSendFeignClient systemMessageSendFeignClient;

	public AuthMessageServiceImpl(RedisTemplate<String, Object> redisTemplate, UserMapper userMapper,
			LoginAuditSnapshotFactory loginAuditSnapshotFactory,
			SystemMessageSendFeignClient systemMessageSendFeignClient) {
		this.redisTemplate = redisTemplate;
		this.userMapper = userMapper;
		this.loginAuditSnapshotFactory = loginAuditSnapshotFactory;
		this.systemMessageSendFeignClient = systemMessageSendFeignClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendEmailCode(String email, HttpServletRequest request) {
		String redisKey = EMAIL_CODE.key(email);
		String code = RandomUtil.randomNumbers(6);

		// 存入 Redis 时使用 MD5，登录校验侧对比 digestHex16
		redisTemplate.opsForValue().set(redisKey, MD5.create().digestHex16(code), EMAIL_CODE.getDefaultTtl());

		// 过期时间转成分钟
		long expireMinutes = EMAIL_CODE.getDefaultTtl().toMinutes();

		// IP地址等信息
		LoginAuditSnapshot snapshot = loginAuditSnapshotFactory.build(request);

		// 查询用户名
		// FIXME 会为空
		UserEntity user = userMapper.selectUserByEmail(email);
		String username = user.getUsername();

		// 拼装发送对象
		Map<String, Object> variables = LoginEmailVariables.build(username, code, expireMinutes, LocalDateTime.now(),
				snapshot.loginIp(), snapshot.loginRegion());
		TemplateMessageCommand command = TemplateMessageCommand.builder()
			.channel(MessageChannel.EMAIL)
			.templateCode(AuthEmailTemplateCode.LOGIN_EMAIL_CODE.getTemplateCode())
			.targets(List.of(email))
			.variables(variables)
			.emailOptions(EmailChannelOptions.builder().hasHtml(Boolean.TRUE).build())
			.build();

		systemMessageSendFeignClient.sendByTemplate(command);
	}

}
