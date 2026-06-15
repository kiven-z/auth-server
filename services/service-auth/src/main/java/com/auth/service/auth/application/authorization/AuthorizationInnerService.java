package com.auth.service.auth.application.authorization;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 授权域内部查询编排
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AuthorizationInnerService {

	private final AuthProfileQueryService authProfileQueryService;

	/**
	 * 构建用户授权画像
	 * @param userId 用户 ID
	 * @return 授权画像，用户不存在等业务失败时由 {@link AuthProfileQueryService} 抛异常
	 */
	public AuthProfile resolveAuthProfile(Long userId) {
		return authProfileQueryService.buildByUserId(userId);
	}

}
