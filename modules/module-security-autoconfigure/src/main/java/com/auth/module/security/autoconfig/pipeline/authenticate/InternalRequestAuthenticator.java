package com.auth.module.security.autoconfig.pipeline.authenticate;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.autoconfig.service.AuthProfileCacheService;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.facade.SecurityTokenHandlerFacade;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 内部JWT认证
 *
 * @author Bunny
 */
public final class InternalRequestAuthenticator implements RequestAuthenticator {

	private final SecurityTokenHandlerFacade tokenHandlerFacade;

	private final AuthProfileCacheService authProfileCacheService;

	public InternalRequestAuthenticator(SecurityTokenHandlerFacade tokenHandlerFacade,
			AuthProfileCacheService authProfileCacheService) {
		this.tokenHandlerFacade = tokenHandlerFacade;
		this.authProfileCacheService = authProfileCacheService;
	}

	/**
	 * 支持请求
	 * @param request HTTP 请求
	 * @return 是否支持
	 */
	@Override
	public boolean supports(HttpServletRequest request) {
		return CharSequenceUtil.isNotBlank(request.getHeader(SecurityInternalTokenConstants.INTERNAL_HEADER));
	}

	/**
	 * 认证请求
	 * @param request HTTP 请求
	 * @return 授权画像
	 */
	@Override
	public AuthProfile authenticate(HttpServletRequest request) {
		// 获取内部的Token
		String internal = request.getHeader(SecurityInternalTokenConstants.INTERNAL_HEADER).trim();
		SecurityTokenResult result = tokenHandlerFacade.validateInternal(internal);

		// 当前是内部交给内部进行验证
		if (SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE.equals(result.getPrincipalType())) {
			return buildServiceProfile(result.getServiceId());
		}

		// 如果内部Token传递了用户信息，进行用户信息的解析
		JwtUserToken userToken = result.getUserToken();
		Long userId = userToken.getUserId();

		// 在传递内部Token时，若是内部用户请求，会携带当前用户信息
		if (userId == null) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID,
					"Internal token user id is missing.");
		}
		AuthProfile profile = authProfileCacheService.load(userId);
		if (profile == null) {
			throw new SecurityTokenException(SecurityResultCodeEnum.SESSION_MISSING, "Auth profile is missing.");
		}
		return profile;
	}

	/**
	 * 构造服务身份的最小化 AuthProfile：不绑定 userId/sessionId/permVersion，仅承载内部服务身份
	 */
	private AuthProfile buildServiceProfile(String serviceId) {
		String username = CharSequenceUtil.isNotBlank(serviceId) ? serviceId : "unknown-service";
		return AuthProfile.builder()
			.username(username)
			.roles(List.of(SecurityInternalTokenConstants.ROLE_INTERNAL_SERVICE))
			.build();
	}

}
