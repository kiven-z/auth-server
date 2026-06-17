package com.auth.service.auth.infrastructure.redis.session.support;

import static com.auth.module.security.contract.redis.SecurityRedisKey.USER_SESSION;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import com.auth.module.security.contract.api.UserSessionIndex;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;

/**
 * 会话索引 Redis Hash 与 {@link UserSessionIndex} 互转；infrastructure: Map↔Bean
 *
 * @author Bunny
 */
@Component
public class UserSessionIndexRedisCodec {

	/**
	 * 将 Redis Hash entries 转为纯字符串 Map（Spring HashOperations 返回 Map&lt;Object, Object&gt;）
	 * @param raw 原始 Hash
	 * @return field/value 均为字符串的 Map
	 */
	public Map<String, String> asStringHash(Map<Object, Object> raw) {
		if (CollUtil.isEmpty(raw)) {
			return Collections.emptyMap();
		}
		Map<String, String> normalized = new LinkedHashMap<>(raw.size());
		raw.forEach((key, value) -> {
			if (key != null && value != null) {
				normalized.put(String.valueOf(key), String.valueOf(value));
			}
		});
		return normalized;
	}

	/**
	 * 从 Hash 组装会话索引
	 * @param jti 会话唯一标识
	 * @param hash 会话 Hash（field/value 均为纯字符串）
	 * @param overrideUserId 覆盖 userId（列表场景传入；单条加载时为 null）
	 * @return 会话索引
	 */
	public UserSessionIndex toSessionIndex(String jti, Map<String, String> hash, Long overrideUserId) {
		UserSessionIndex sessionIndex = BeanUtil.toBean(normalizeLegacyHash(hash), UserSessionIndex.class);
		if (overrideUserId != null) {
			sessionIndex.setUserId(overrideUserId);
		}
		sessionIndex.setSessionId(jti);
		return sessionIndex;
	}

	/**
	 * 构建 register Lua 脚本参数（纯字符串 ARGV，配合 sessionRedisTemplate）
	 * @param userSessionIndex 会话索引
	 * @param sessionId 会话 ID
	 * @param loginAtScore 登录时间分值
	 * @param ttl 会话 TTL
	 * @return ARGV 数组（必须为 Object[]，避免 String[] 传入 varargs 被当作单参数）
	 */
	public Object[] buildRegisterScriptArgs(UserSessionIndex userSessionIndex, String sessionId, double loginAtScore,
			Duration ttl) {
		List<String> args = new ArrayList<>();
		args.add(sessionId);
		args.add(String.valueOf((long) loginAtScore));
		args.add(String.valueOf(ttl != null ? ttl.getSeconds() : 0L));
		args.add(String.valueOf(userSessionIndex.getUserId()));

		Map<String, Object> sessionMap = BeanUtil.beanToMap(userSessionIndex);
		sessionMap.forEach((field, value) -> {
			if (value != null) {
				args.add(field);
				args.add(String.valueOf(value));
			}
		});
		return args.toArray(new Object[0]);
	}

	/**
	 * 兼容旧数据：Lua + JSON 序列化 ARGV 曾把 field 名写成带引号字面量
	 * @param hash 原始 Hash
	 * @return 规范化后的 Hash
	 */
	private Map<String, String> normalizeLegacyHash(Map<String, String> hash) {
		Map<String, String> normalized = new LinkedHashMap<>(hash.size());
		hash.forEach((key, value) -> normalized.put(unwrapQuoted(key), unwrapQuoted(value)));
		return normalized;
	}

	/**
	 * 去掉 Redis 中因 JSON 序列化残留的首尾双引号
	 * @param raw 原始字符串
	 * @return 去引号后的值
	 */
	private String unwrapQuoted(String raw) {
		if (CharSequenceUtil.isBlank(raw)) {
			return raw;
		}
		if (raw.length() >= 2 && raw.charAt(0) == '"' && raw.charAt(raw.length() - 1) == '"') {
			return raw.substring(1, raw.length() - 1);
		}
		return raw;
	}

	/**
	 * 根据 refreshToken 过期时间计算会话 TTL
	 * @param userSessionIndex 会话索引
	 * @return TTL，无法计算时返回默认 TTL
	 */
	public Duration resolveSessionTtl(@NotNull UserSessionIndex userSessionIndex) {
		// 获取刷新令牌过期时间
		Long refreshTokenExpiresAt = userSessionIndex.getRefreshTokenExpiresAt();
		Duration ttl = USER_SESSION.getDefaultTtl();

		// 计算会话 TTL
		if (refreshTokenExpiresAt != null) {
			long ttlSeconds = (refreshTokenExpiresAt - System.currentTimeMillis()) / 1000;
			if (ttlSeconds > 0) {
				ttl = Duration.ofSeconds(ttlSeconds);
			}
		}
		return ttl;
	}

	/**
	 * 解析在线用户 ZSet 排序分值
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
