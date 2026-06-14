package com.auth.service.auth.session.infrastructure.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.infrastructure.redis.script.UserSessionRedisScripts;
import com.auth.service.auth.session.infrastructure.redis.support.UserSessionIndexRedisCodec;
import org.jetbrains.annotations.NotNull;
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

	private final RedisTemplate<String, Object> redisTemplate;

	private final UserSessionRedisScripts userSessionRedisScripts;

	private final UserSessionIndexRedisCodec userSessionIndexRedisCodec;

	public UserSessionLifecycleStore(RedisTemplate<String, Object> redisTemplate,
			UserSessionRedisScripts userSessionRedisScripts, UserSessionIndexRedisCodec userSessionIndexRedisCodec) {
		this.redisTemplate = redisTemplate;
		this.userSessionRedisScripts = userSessionRedisScripts;
		this.userSessionIndexRedisCodec = userSessionIndexRedisCodec;
	}

	/**
	 * 注册活跃会话：写入 Hash、用户 Set 与全局 ZSet（Lua 原子）
	 * @param userSessionIndex 用户会话索引
	 */
	public void registerSession(@NotNull UserSessionIndex userSessionIndex) {
		String sessionId = userSessionIndex.getSessionId();
		Long userId = userSessionIndex.getUserId();
		if (CharSequenceUtil.isBlank(sessionId) || userId == null) {
			throw new IllegalArgumentException("sessionId and userId are required for session registration");
		}

		List<String> keys = List.of(USER_SESSION.key(sessionId), USER_SESSIONS.key(userId), GLOBAL_SESSIONS.fixedKey());
		Object[] args = userSessionIndexRedisCodec.buildRegisterScriptArgs(userSessionIndex, sessionId,
				userSessionIndexRedisCodec.resolveLoginAtScore(userSessionIndex),
				userSessionIndexRedisCodec.resolveSessionTtl(userSessionIndex));
		redisTemplate.execute(userSessionRedisScripts.registerScript(), keys, args);
	}

	/**
	 * 终止单条会话：删除 Hash、用户 Set 与全局 ZSet（Lua 原子，幂等）
	 * @param userId 用户 ID
	 * @param jti 会话唯一标识
	 */
	public void terminateSession(long userId, @NotNull String jti) {
		if (CharSequenceUtil.isBlank(jti)) {
			return;
		}
		List<String> keys = List.of(USER_SESSION.key(jti), USER_SESSIONS.key(userId), GLOBAL_SESSIONS.fixedKey());
		redisTemplate.execute(userSessionRedisScripts.terminateScript(), keys, jti);
	}

	/**
	 * 终止用户全部活跃会话
	 * @param userId 用户 ID
	 */
	public void terminateAllSessions(long userId) {
		String userSessionsKey = USER_SESSIONS.key(userId);
		List<String> jtis = streamActiveJtis(userSessionsKey).toList();
		redisTemplate.delete(userSessionsKey);
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
		Map<Object, Object> hash = redisTemplate.opsForHash().entries(USER_SESSION.key(jti));
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
		redisTemplate.opsForHash().put(sessionKey, "refreshTokenHash", refreshTokenHash);
		redisTemplate.opsForHash().put(sessionKey, "refreshTokenExpiresAt", refreshTokenExpiresAt);
		redisTemplate.expire(sessionKey, ttl);
	}

	/**
	 * 从 Hash 解析 userId 后终止；Hash 已丢失时仍清理全局 ZSet
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
		List<String> keys = List.of(USER_SESSION.key(jti), GLOBAL_SESSIONS.fixedKey());
		redisTemplate.execute(userSessionRedisScripts.terminateOrphanScript(), keys, jti);
	}

	/**
	 * 用户活跃会话 Set 中的 jti 流
	 * @param userSessionsKey 用户会话 Set key
	 * @return 非空 jti 流
	 */
	private Stream<String> streamActiveJtis(String userSessionsKey) {
		return Optional.ofNullable(redisTemplate.opsForSet().members(userSessionsKey))
			.orElseGet(Collections::emptySet)
			.stream()
			.filter(String.class::isInstance)
			.map(String.class::cast)
			.filter(CharSequenceUtil::isNotBlank);
	}

}
