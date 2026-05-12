package com.auth.service.auth.service.impl;

import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.common.web.model.entity.UserAgent;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.auth.mapper.LoginLogMapper;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.service.AuthLoginAuditService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录与安全事件审计持久化实现。
 *
 * @author Bunny
 */
@Slf4j
@Service
public class AuthLoginAuditServiceImpl extends ServiceImpl<LoginLogMapper, LoginLogEntity>
		implements AuthLoginAuditService {

	private final IpAddressService ipAddressService;

	public AuthLoginAuditServiceImpl(IpAddressService ipAddressService) {
		this.ipAddressService = ipAddressService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordLoginLog(HttpServletRequest request, AuthLoginLogType eventType, AuthLoginLogResult result,
			Long userId, String principal, String failureReason, String sessionId) {
		LoginLogEntity entity = new LoginLogEntity();

		if (request != null) {
			IpInfo ipInfo = ipAddressService.resolveIpInfo(request);
			UserAgent userAgent = UserAgent.getUserAgent(request);
			entity.setLoginIp(ipInfo.getIpAddr());
			entity.setLoginRegion(ipInfo.getIpRegion());
			entity.setUserAgent(request.getHeader("User-Agent"));
			entity.setDeviceType(userAgent.getDeviceType());
			entity.setOsType(userAgent.getOs());
			entity.setBrowserType(userAgent.getBrowser());
		}

		try {
			entity.setUserId(userId);
			entity.setUsername(principal);
			entity.setLoginType(eventType.name());
			entity.setLoginResult(result.getCode());
			entity.setFailureReason(failureReason);
			entity.setLoginTime(LocalDateTime.now());
			entity.setSessionId(sessionId);

			// 设置更新用户和创建用户，因为部分登录不存在用户信息
			entity.setCreateUser(userId);
			entity.setUpdateUser(userId);
			entity.setCreateTime(LocalDateTime.now());
			entity.setUpdateTime(LocalDateTime.now());
			baseMapper.insert(entity);
		}
		catch (Exception ex) {
			log.error(
					"Failed to persist auth audit: eventType={}, result={}, userId={}, principal={}, sessionId={}, cause={}",
					eventType, result, userId, principal, sessionId, ex, ex);
		}
	}

}
