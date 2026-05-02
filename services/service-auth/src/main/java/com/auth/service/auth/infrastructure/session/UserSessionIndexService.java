package com.auth.service.auth.infrastructure.session;

import cn.hutool.crypto.digest.MD5;
import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.common.jwt.autoconfigure.JwtProperties;
import com.auth.common.web.model.entity.UserAgent;
import com.auth.module.security.contract.api.UserSessionIndex;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * 负责创建 UserSessionIndex
 *
 * @author Bunny
 */
@Service
public class UserSessionIndexService {

	private final IpAddressService ipAddressService;

	private final JwtProperties properties;

	public UserSessionIndexService(IpAddressService ipAddressService, JwtProperties properties) {
		this.ipAddressService = ipAddressService;
		this.properties = properties;
	}

	/**
	 * 构建用户会话索引
	 */
	public UserSessionIndex buildUserSessionIndex(HttpServletRequest request, Long userId, String jti,
			String refreshToken) {
		IpInfo ipInfo = ipAddressService.resolveIpInfo(request);
		UserAgent userAgent = UserAgent.getUserAgent(request);

		// 构建会话索引
		UserSessionIndex userSessionIndex = new UserSessionIndex();
		userSessionIndex.setUserId(userId);
		userSessionIndex.setSessionId(jti);
		userSessionIndex.setIpAddress(ipInfo.getIpAddr());
		userSessionIndex.setIpRegion(ipInfo.getIpRegion());
		userSessionIndex.setDeviceType(userAgent.getDeviceType());
		userSessionIndex.setBrowserType(userAgent.getBrowser());
		userSessionIndex.setOsType(userAgent.getOs());

		// 设置刷新TokenHash等
		String refreshTokenHash = MD5.create().digestHex16(refreshToken);
		userSessionIndex.setRefreshTokenHash(refreshTokenHash);
		long refreshExpiredSeconds = properties.getRefreshExpired();
		long refreshTokenExpiresAtMillis = System.currentTimeMillis() + refreshExpiredSeconds * 1000L;
		userSessionIndex.setRefreshTokenExpiresAt(refreshTokenExpiresAtMillis);
		return userSessionIndex;
	}

}
