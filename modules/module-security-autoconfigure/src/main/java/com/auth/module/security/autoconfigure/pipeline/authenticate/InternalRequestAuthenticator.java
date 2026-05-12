package com.auth.module.security.autoconfigure.pipeline.authenticate;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.constants.SecurityInternalTokenConstants;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import com.auth.module.security.core.token.provider.InternalTokenProvider;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 内部JWT认证
 *
 * @author Bunny
 */
public final class InternalRequestAuthenticator implements RequestAuthenticator {

	private final InternalTokenProvider internalTokenProvider;

	public InternalRequestAuthenticator(InternalTokenProvider internalTokenProvider) {
		this.internalTokenProvider = internalTokenProvider;
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
		SecurityTokenResult result = internalTokenProvider.parseToken(internal);

		// 仅允许服务身份的内部令牌访问 @InternalApi
		if (!SecurityInternalTokenConstants.PRINCIPAL_TYPE_SERVICE.equals(result.getPrincipalType())) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID,
					"Internal endpoint requires service principal token.");
		}
		return buildServiceProfile(result.getServiceId());
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
