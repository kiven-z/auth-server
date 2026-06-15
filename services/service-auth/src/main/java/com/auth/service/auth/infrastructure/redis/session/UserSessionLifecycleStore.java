package com.auth.service.auth.infrastructure.redis.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.infrastructure.redis.session.script.UserSessionRedisScripts;
import com.auth.service.auth.infrastructure.redis.session.support.UserSessionIndexRedisCodec;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.auth.module.security.contract.redis.SecurityRedisKey.*;

/**
 * 用户会话生命周期：注册、终止、单条加载与 refresh 信息更新
 *
 * @author Bunny
 */
@Service
public class UserSessionLifecycleStore {

	private final RedisTemplate<String, String> sessionRedisTemplate;

	private final UserSessionRedisScripts userSessionRedisScripts;

	private final UserSessionIndexRedisCodec userSessionIndexRedisCodec;

	public UserSessionLifecycleStore(
			@Qualifier("sessionRedisTemplate") RedisTemplate<String, String> sessionRedisTemplate,
			UserSessionRedisScripts userSessionRedisScripts, UserSessionIndexRedisCodec userSessionIndexRedisCodec) {
		this.sessionRedisTemplate = sessionRedisTemplate;
		this.userSessionRedisScripts = userSessionRedisScripts;
		this.userSessionIndexRedisCodec = userSessionIndexRedisCodec;
	}

	/**
	 * 注册活跃会话：写入 Hash、用户 Set 与在线用户 ZSet（Lua 原子）
	 * @param userSessionIndex 用户会话索引
	 */
	public void registerSession(@NotNull UserSessionIndex userSessionIndex) {
		String sessionId = userSessionIndex.getSessionId();
		Long userId = userSessionIndex.getUserId();
		if (CharSequenceUtil.isBlank(sessionId) || userId == null) {
			throw new IllegalArgumentException("sessionId and userId are required for session registration");
		}

		List<String> keys = List.of(USER_SESSION.key(sessionId), USER_SESSIONS.key(userId), ONLINE_USERS.fixedKey());
		Object[] scriptArgs = userSessionIndexRedisCodec.buildRegisterScriptArgs(userSessionIndex, sessionId,
				userSessionIndexRedisCodec.resolveLoginAtScore(userSessionIndex),
				userSessionIndexRedisCodec.resolveSessionTtl(userSessionIndex));
		sessionRedisTemplate.execute(userSessionRedisScripts.registerScript(), keys, scriptArgs);
	}

	/**
	 * 终止单条会话：删除 Hash 与用户 Set（Lua 原子，幂等）；在线用户 ZSet 由 Java 侧按 SCARD 同步
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(long userId, @NotNull String jti) {
		if (CharSequenceUtil.isBlank(jti)) {
			return;
		}
		List<String> keys = List.of(USER_SESSION.key(jti), USER_SESSIONS.key(userId));
		sessionRedisTemplate.execute(userSessionRedisScripts.terminateScript(), keys, jti);
	}

	/**
	 * 终止用户全部活跃会话
	 * @param userId 用户 ID
	 */
	public void terminateAllSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		List<String> jtis = streamActiveJtis(userSessionsKey).toList();
		sessionRedisTemplate.delete(userSessionsKey);
		jtis.forEach(jti -> terminateSession(userId, jti));
	}

	/**
	 * 读取会话索引（UserSessionIndex）（不存在返回空）
	 * @param jti 会话唯一标识
	 * @return 会话索引
	 */
	public Optional<UserSessionIndex> loadUserSessionIndex(@NotNull String jti) {
		if (CharSequenceUtil.isBlank(jti)) {
			return Optional.empty();
		}
		Map<String, String> hash = userSessionIndexRedisCodec
			.asStringHash(sessionRedisTemplate.opsForHash().entries(USER_SESSION.key(jti)));
		if (CollUtil.isEmpty(hash)) {
			return Optional.empty();
		}
		return Optional.of(userSessionIndexRedisCodec.toSessionIndex(jti, hash, null));
	}

	/**
	 * 更新刷新令牌校验信息（Hash字段）并刷新会话 TTL
	 * @param jti 会话唯一标识
	 * @param refreshTokenHash refreshToken 哈希
	 * @param refreshTokenExpiresAt refreshToken 过期时间戳（毫秒）
	 * @param ttl 会话 TTL（通常与 refreshToken 有效期一致）
	 */
	public void updateRefreshInfo(@NotNull String jti, @NotNull String refreshTokenHash, long refreshTokenExpiresAt,
			@NotNull Duration ttl) {
		String sessionKey = USER_SESSION.key(jti);
		sessionRedisTemplate.opsForHash().put(sessionKey, "refreshTokenHash", refreshTokenHash);
		sessionRedisTemplate.opsForHash()
			.put(sessionKey, "refreshTokenExpiresAt", String.valueOf(refreshTokenExpiresAt));
		sessionRedisTemplate.expire(sessionKey, ttl);
	}

	/**
	 * 从 Hash 解析 userId 后终止；Hash 已丢失时仅删除残留 Hash key
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(@NotNull String jti) {
		if (CharSequenceUtil.isBlank(jti)) {
			return;
		}
		Optional<UserSessionIndex> sessionIndex = loadUserSessionIndex(jti);
		if (sessionIndex.isPresent() && sessionIndex.get().getUserId() != null) {
			terminateSession(sessionIndex.get().getUserId(), jti);
			return;
		}
		List<String> keys = List.of(USER_SESSION.key(jti));
		sessionRedisTemplate.execute(userSessionRedisScripts.terminateOrphanScript(), keys, jti);
	}

	/**
	 * 用户活跃会话 Set 中的 jti 流
	 * @param userSessionsKey 用户会话 Set key
	 * @return 非空 jti 流
	 */
	private Stream<String> streamActiveJtis(String userSessionsKey) {
		return Optional.ofNullable(sessionRedisTemplate.opsForSet().members(userSessionsKey))
			.orElseGet(Collections::emptySet)
			.stream()
			.filter(CharSequenceUtil::isNotBlank);
	}

}
