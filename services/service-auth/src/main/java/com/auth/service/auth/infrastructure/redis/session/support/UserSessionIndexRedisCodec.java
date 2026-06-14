package com.auth.service.auth.infrastructure.redis.session.support;

import cn.hutool.core.bean.BeanUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSION;

/**
 * 会话索引 Redis Hash 与 {@link UserSessionIndex} 互转；infrastructure: Map↔Bean
 *
 * @author Bunny
 */
@Component
public class UserSessionIndexRedisCodec {

	/**
	 * 从 Hash 组装会话索引
	 * @param jti 会话唯一标识
	 * @param hash 会话 Hash
	 * @param overrideUserId 覆盖 userId（列表场景传入；单条加载时为 null）
	 * @return 会话索引
	 */
	public UserSessionIndex toSessionIndex(String jti, Map<Object, Object> hash, Long overrideUserId) {
		UserSessionIndex sessionIndex = BeanUtil.toBean(hash, UserSessionIndex.class);
		if (overrideUserId != null) {
			sessionIndex.setUserId(overrideUserId);
		}
		sessionIndex.setSessionId(jti);
		return sessionIndex;
	}

	/**
	 * 构建 register Lua 脚本参数
	 * @param userSessionIndex 会话索引
	 * @param sessionId 会话 ID
	 * @param loginAtScore 登录时间分值
	 * @param ttl 会话 TTL
	 * @return ARGV 数组
	 */
	public Object[] buildRegisterScriptArgs(UserSessionIndex userSessionIndex, String sessionId, double loginAtScore,
			Duration ttl) {
		List<Object> args = new ArrayList<>();
		args.add(sessionId);
		args.add(loginAtScore);
		args.add(ttl != null ? ttl.getSeconds() : 0L);

		Map<String, Object> sessionMap = BeanUtil.beanToMap(userSessionIndex);
		sessionMap.forEach((field, value) -> {
			if (value != null) {
				args.add(field);
				args.add(String.valueOf(value));
			}
		});
		return args.toArray();
	}

	/**
	 * 根据 refreshToken 过期时间计算会话 TTL
	 * @param userSessionIndex 会话索引
	 * @return TTL，无法计算时返回默认 TTL
	 */
	public Duration resolveSessionTtl(@NotNull UserSessionIndex userSessionIndex) {
		Long refreshTokenExpiresAt = userSessionIndex.getRefreshTokenExpiresAt();
		Duration ttl = USER_SESSION.getDefaultTtl();
		if (refreshTokenExpiresAt != null) {
			long ttlSeconds = (refreshTokenExpiresAt - System.currentTimeMillis()) / 1000;
			if (ttlSeconds > 0) {
				ttl = Duration.ofSeconds(ttlSeconds);
			}
		}
		return ttl;
	}

	/**
	 * 解析全局 ZSet 排序分值
	 * @param userSessionIndex 会话索引
	 * @return loginAt 毫秒时间戳
	 */
	public double resolveLoginAtScore(@NotNull UserSessionIndex userSessionIndex) {
		Long loginAt = userSessionIndex.getLoginAt();
		if (loginAt != null) {
			return loginAt.doubleValue();
		}
		return System.currentTimeMillis();
	}

}
