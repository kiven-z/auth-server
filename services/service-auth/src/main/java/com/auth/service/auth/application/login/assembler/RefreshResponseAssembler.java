package com.auth.service.auth.application.login.assembler;

import com.auth.service.auth.api.dto.authentication.response.RefreshTokenResponse;
import com.auth.service.auth.domain.authentication.model.RefreshSessionResult;
import lombok.experimental.UtilityClass;

/**
 * 将领域刷新结果组装为 API 响应。
 *
 * @author Bunny
 */
@UtilityClass
public class RefreshResponseAssembler {

	/**
	 * 由 {@link RefreshSessionResult} 构建刷新令牌 API 响应（尚未填充 readMeDay）。
	 * @param result 领域刷新结果
	 * @return API 刷新响应
	 */
	public static RefreshTokenResponse toRefreshTokenResponse(RefreshSessionResult result) {
		RefreshTokenResponse response = new RefreshTokenResponse();
		response.setAccessToken(result.tokenPair().accessToken());
		response.setRefreshToken(result.tokenPair().refreshToken());
		response.setExpires(result.tokenPair().accessExpiresAt());
		return response;
	}

}
