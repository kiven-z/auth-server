package com.auth.service.auth.application.authorization.command;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.application.port.authorization.AuthProfileCachePort;
import com.auth.service.auth.application.port.authorization.AuthProfileQueryPort;
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

	private final AuthProfileQueryPort authProfileQueryPort;

	private final AuthProfileCachePort authProfileCachePort;

	/**
	 * 按用户 ID 加载授权画像（供内部接口查询生效角色码与权限码）
	 * @param userId 用户 ID
	 * @return 授权画像
	 */
	public AuthProfile loadAuthProfile(Long userId) {
		return authProfileQueryPort.buildByUserId(userId);
	}

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
				AuthProfile profile = authProfileQueryPort.buildByUserId(userId);
				authProfileCachePort.cacheProfile(profile);
			}
			catch (RuntimeException ex) {
				log.warn("Failed to refresh auth profile cache for userId={}: {}", userId, ex.getMessage());
			}
		});
	}

}
