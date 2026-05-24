package com.auth.service.auth.authorization.application;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authorization.api.dto.EffectiveCodesVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 授权画像缓存刷新：按用户重建并写入 Redis。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthorizationInnerService {

	private final AuthProfileQueryService authProfileQueryService;

	private final AuthProfileCacheApplicationService authProfileCacheApplicationService;

	/**
	 * 刷新用户授权画像缓存。
	 * @param userIds 用户 ID 列表
	 */
	public void refreshUserCache(Collection<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return;
		}

		userIds.stream().filter(Objects::nonNull).forEach(userId -> {
			try {
				AuthProfile profile = authProfileQueryService.buildByUserId(userId);
				authProfileCacheApplicationService.cacheProfile(profile);
			}
			catch (RuntimeException ex) {
				log.warn("Failed to refresh auth profile cache for userId={}: {}", userId, ex.getMessage());
			}
		});
	}

	/**
	 * 解析用户生效角色码与权限码。
	 * @param userId 用户 ID
	 * @return 角色码与权限码
	 */
	public EffectiveCodesVO resolveEffectiveCodes(Long userId) {
		// 构建用户授权画像
		AuthProfile profile = authProfileQueryService.buildByUserId(userId);

		// 如果用户授权画像为空，则返回空角色码和权限码
		EffectiveCodesVO vo = new EffectiveCodesVO();
		if (profile == null) {
			vo.setRoleCodes(List.of());
			vo.setPermissionCodes(List.of());
			return vo;
		}

		// 设置角色码和权限码
		vo.setRoleCodes(Objects.requireNonNullElse(profile.getRoles(), List.of()));
		vo.setPermissionCodes(Objects.requireNonNullElse(profile.getPermissions(), List.of()));
		return vo;
	}

}
