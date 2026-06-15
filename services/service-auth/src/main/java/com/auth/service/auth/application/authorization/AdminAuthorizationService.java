package com.auth.service.auth.application.authorization;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.application.port.login.AuthProfileCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

/**
 * 管理端授权画像运维：按用户重建并写入 Redis
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AdminAuthorizationService {

	private final AuthProfileQueryService authProfileQueryService;

	private final AuthProfileCachePort authProfileCachePort;

	/**
	 * 刷新用户授权画像缓存
	 * @param userIds 用户 ID 列表
	 */
	public void refreshUserCache(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return;
		}

		userIds.stream().filter(Objects::nonNull).forEach(userId -> {
			try {
				AuthProfile profile = authProfileQueryService.buildByUserId(userId);
				authProfileCachePort.cacheProfile(profile);
			}
			catch (RuntimeException ex) {
				log.warn("Failed to refresh auth profile cache for userId={}: {}", userId, ex.getMessage());
			}
		});
	}

}
