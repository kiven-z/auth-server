package com.auth.service.auth.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.model.vo.authorization.EffectiveCodesVO;
import com.auth.service.auth.service.AdminAuthorizationService;
import com.auth.service.auth.support.authorization.AuthProfileRepository;
import com.auth.service.auth.support.redis.AuthProfileRedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 管理端授权画像运维：按用户重建并写入 Redis
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AdminAuthorizationServiceImpl implements AdminAuthorizationService {

	private final AuthProfileRepository authProfileRepository;

	private final AuthProfileRedisCache authProfileRedisCache;

	/**
	 * 按用户 ID 加载授权画像（供内部接口查询生效角色码与权限码）
	 * @param userId 用户 ID
	 * @return 授权画像
	 */
	@Override
	public AuthProfile loadAuthProfile(Long userId) {
		return authProfileRepository.buildByUserId(userId);
	}

	/**
	 * 查询用户生效角色码与权限码
	 * @param userId 用户 ID
	 * @return 生效码 VO
	 */
	@Override
	public EffectiveCodesVO getEffectiveCodes(Long userId) {
		AuthProfile profile = authProfileRepository.buildByUserId(userId);

		if (profile == null) {
			EffectiveCodesVO vo = new EffectiveCodesVO();
			vo.setRoleCodes(List.of());
			vo.setPermissionCodes(List.of());
			return vo;
		}

		EffectiveCodesVO vo = new EffectiveCodesVO();
		vo.setRoleCodes(profile.getRoles());
		vo.setPermissionCodes(profile.getPermissions());
		return vo;
	}

	/**
	 * 刷新用户授权画像缓存
	 * @param userIds 用户 ID 列表
	 */
	@Override
	public void refreshUserCache(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return;
		}

		userIds.stream().filter(Objects::nonNull).forEach(userId -> {
			try {
				AuthProfile profile = authProfileRepository.buildByUserId(userId);
				authProfileRedisCache.cacheProfile(profile);
			}
			catch (RuntimeException ex) {
				log.warn("Failed to refresh auth profile cache for userId={}: {}", userId, ex.getMessage());
			}
		});
	}

}
