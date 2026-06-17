package com.auth.service.auth.convert;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.auth.api.dto.authentication.response.RefreshTokenResponse;
import com.auth.service.auth.api.dto.authentication.response.UserLoginResponse;
import com.auth.service.auth.domain.authentication.model.CompletedLoginSession;
import com.auth.service.auth.domain.authentication.model.CompletedRefreshSession;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 登录/刷新会话读模型 → HTTP 响应。
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface LoginSessionConverter {

	LoginSessionConverter INSTANCE = Mappers.getMapper(LoginSessionConverter.class);

	/**
	 * 登录会话读模型 → API 登录响应。
	 * @param session 已签发登录会话
	 * @return HTTP 登录响应
	 */
	UserLoginResponse toUserLoginResponse(CompletedLoginSession session);

	/**
	 * 刷新会话读模型 → API 刷新响应。
	 * @param session 已刷新会话
	 * @return HTTP 刷新响应
	 */
	RefreshTokenResponse toRefreshTokenResponse(CompletedRefreshSession session);

}
