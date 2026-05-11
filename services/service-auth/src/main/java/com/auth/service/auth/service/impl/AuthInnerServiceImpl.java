package com.auth.service.auth.service.impl;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.infrastructure.profile.AuthProfileCache;
import com.auth.service.auth.infrastructure.profile.AuthProfileService;
import com.auth.service.auth.infrastructure.session.UserSessionIndexSession;
import com.auth.service.auth.model.vo.inner.EffectiveCodesVO;
import com.auth.service.auth.service.AuthInnerService;
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
public class AuthInnerServiceImpl implements AuthInnerService {

	private final UserSessionIndexSession userSessionIndexSession;

	private final AuthProfileService authProfileService;

	private final AuthProfileCache authProfileCache;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refreshUserCache(List<Long> userIds) {
		userIds.stream().filter(Objects::nonNull).forEach(userId -> {
			try {
				AuthProfile profile = authProfileService.buildByUserId(userId);
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
				userSessionIndexSession.deleteAllSessions(userId);
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
		AuthProfile profile = authProfileService.buildByUserId(userId);
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
