package com.auth.service.auth.integration.application;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authorization.api.dto.EffectiveCodesVO;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.authorization.infrastructure.cache.AuthProfileCache;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 内部认证服务
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class InternalAuthCoordinationService {

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	private final AuthProfileQueryService authProfileQueryService;

	private final AuthProfileCache authProfileCache;

	/**
	 * 刷新用户缓存
	 * @param userIds 用户ID列表
	 */
	public void refreshUserCache(List<Long> userIds) {
		userIds.stream().filter(Objects::nonNull).forEach(userId -> {
			try {
				AuthProfile profile = authProfileQueryService.buildByUserId(userId);
				authProfileCache.createCacheAuthProfile(profile);
			}
			catch (RuntimeException ex) {
				log.warn("Failed to refresh auth profile cache for userId={}: {}", userId, ex.getMessage());
			}
		});
	}

	/**
	 * 踢出所有会话
	 * @param userIds 用户ID列表
	 */
	public void kickAllSessions(List<Long> userIds) {
		userIds.stream().filter(Objects::nonNull).forEach(userId -> {
			try {
				redisUserSessionIndexRepository.deleteAllSessions(userId);
			}
			catch (RuntimeException ex) {
				log.warn("Failed to kick all sessions for userId={}: {}", userId, ex.getMessage());
			}
		});
	}

	/**
	 * 解析用户生效角色码与权限码（与登录画像构建逻辑一致，含管理员展开）
	 * @param userId 用户 ID
	 * @return 角色码与权限码
	 */
	public EffectiveCodesVO resolveEffectiveCodes(Long userId) {
		AuthProfile profile = authProfileQueryService.buildByUserId(userId);
		EffectiveCodesVO vo = new EffectiveCodesVO();
		if (profile == null) {
			vo.setRoleCodes(List.of());
			vo.setPermissionCodes(List.of());
			return vo;
		}

		vo.setRoleCodes(Objects.requireNonNullElse(profile.getRoles(), List.of()));
		vo.setPermissionCodes(Objects.requireNonNullElse(profile.getPermissions(), List.of()));
		return vo;
	}

}
