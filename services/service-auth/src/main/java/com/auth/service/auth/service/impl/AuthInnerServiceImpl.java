package com.auth.service.auth.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.infrastructure.profile.AuthProfileCache;
import com.auth.service.auth.infrastructure.profile.AuthProfileService;
import com.auth.service.auth.infrastructure.session.UserSessionIndexSession;
import com.auth.service.auth.service.AuthInnerService;

import lombok.extern.slf4j.Slf4j;

/**
 * 内部认证服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
public class AuthInnerServiceImpl implements AuthInnerService {

	private final UserSessionIndexSession userSessionIndexSession;

	private final AuthProfileService authProfileService;

	private final AuthProfileCache authProfileCache;

	public AuthInnerServiceImpl(UserSessionIndexSession userSessionIndexSession, AuthProfileService authProfileService,
			AuthProfileCache authProfileCache) {
		this.userSessionIndexSession = userSessionIndexSession;
		this.authProfileService = authProfileService;
		this.authProfileCache = authProfileCache;
	}

	/**
	 * 刷新用户缓存
	 * @param userIds 用户ID列表
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
	 * 踢出所有会话
	 * @param userIds 用户ID列表
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

}
