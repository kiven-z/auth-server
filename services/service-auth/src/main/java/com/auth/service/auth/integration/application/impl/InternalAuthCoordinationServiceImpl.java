package com.auth.service.auth.integration.application.impl;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authorization.api.dto.EffectiveCodesVO;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.authorization.infrastructure.cache.AuthProfileCache;
import com.auth.service.auth.integration.application.InternalAuthCoordinationService;
import com.auth.service.auth.session.infrastructure.redis.RedisUserSessionIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 内部认证服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class InternalAuthCoordinationServiceImpl implements InternalAuthCoordinationService {

	private final RedisUserSessionIndexRepository redisUserSessionIndexRepository;

	private final AuthProfileQueryService authProfileQueryService;

	private final AuthProfileCache authProfileCache;

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
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
