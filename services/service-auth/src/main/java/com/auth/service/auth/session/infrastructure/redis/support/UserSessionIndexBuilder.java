package com.auth.service.auth.session.infrastructure.redis.support;

import cn.hutool.crypto.digest.MD5;
import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.web.model.entity.UserAgent;
import com.auth.module.security.contract.api.UserSessionIndex;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * 从 HTTP 请求与令牌参数构建 {@link UserSessionIndex}
 *
 * @author Bunny
 */
@Service
public class UserSessionIndexBuilder {

	private final IpAddressService ipAddressService;

	private final JwtProperties properties;

	public UserSessionIndexBuilder(IpAddressService ipAddressService, JwtProperties properties) {
		this.ipAddressService = ipAddressService;
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	public UserSessionIndex build(@NotNull HttpServletRequest request, Long userId, String jti, String refreshToken,
			boolean rememberMe) {
		IpInfo ipInfo = ipAddressService.resolveIpInfo(request);
		UserAgent userAgent = UserAgent.getUserAgent(request);

		UserSessionIndex userSessionIndex = new UserSessionIndex();
		userSessionIndex.setUserId(userId);
		userSessionIndex.setSessionId(jti);
		userSessionIndex.setIpAddress(ipInfo.getIpAddr());
		userSessionIndex.setIpRegion(ipInfo.getIpRegion());
		userSessionIndex.setDeviceType(userAgent.getDeviceType());
		userSessionIndex.setBrowserType(userAgent.getBrowser());
		userSessionIndex.setOsType(userAgent.getOs());

		String refreshTokenHash = MD5.create().digestHex16(refreshToken);
		userSessionIndex.setRefreshTokenHash(refreshTokenHash);
		long refreshExpiredSeconds = properties.getRefreshExpired();
		long refreshTokenExpiresAtMillis = System.currentTimeMillis() + refreshExpiredSeconds * 1000L;
		userSessionIndex.setRefreshTokenExpiresAt(refreshTokenExpiresAtMillis);
		userSessionIndex.setRememberMe(rememberMe);
		userSessionIndex.setLoginAt(System.currentTimeMillis());
		return userSessionIndex;
	}

}
