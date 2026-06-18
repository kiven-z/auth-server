package com.auth.service.auth.infrastructure.audit.adapter;

import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.common.web.model.entity.UserAgent;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.auth.application.port.login.LoginAuditPort;
import com.auth.service.auth.domain.authentication.model.LoginAuditSnapshot;
import com.auth.service.auth.domain.authentication.model.enums.AuthLoginLogResult;
import com.auth.service.auth.domain.authentication.model.enums.AuthLoginLogType;
import com.auth.service.auth.infrastructure.persistence.mapper.LoginLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 登录审计适配器：解析请求快照并异步写入 {@code log_login}。
 *
 * @author Bunny
 */
@Slf4j
@Repository
public class LoginAuditAdapter extends ServiceImpl<LoginLogMapper, LoginLogEntity> implements LoginAuditPort {

	private final IpAddressService ipAddressService;

	public LoginAuditAdapter(IpAddressService ipAddressService) {
		this.ipAddressService = ipAddressService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LoginAuditSnapshot buildSnapshot(HttpServletRequest request) {
		if (request == null) {
			return LoginAuditSnapshot.empty();
		}
		IpInfo ipInfo = ipAddressService.resolveIpInfo(request);
		UserAgent userAgent = UserAgent.getUserAgent(request);
		return new LoginAuditSnapshot(ipInfo.getIpAddr(), ipInfo.getIpRegion(), request.getHeader("User-Agent"),
				userAgent.getDeviceType(), userAgent.getOs(), userAgent.getBrowser());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Async
	public void recordLoginLog(LoginAuditSnapshot snapshot, AuthLoginLogType eventType, AuthLoginLogResult result,
			Long userId, String principal, String failureReason, String sessionId) {
		LoginLogEntity entity = new LoginLogEntity();
		entity.setLoginIp(snapshot.loginIp());
		entity.setLoginRegion(snapshot.loginRegion());
		entity.setUserAgent(snapshot.userAgent());
		entity.setDeviceType(snapshot.deviceType());
		entity.setOsType(snapshot.osType());
		entity.setBrowserType(snapshot.browserType());

		try {
			entity.setUserId(userId);
			entity.setUsername(principal);
			entity.setLoginType(eventType.name());
			entity.setLoginResult(result.getCode());
			entity.setFailureReason(failureReason);
			entity.setLoginTime(LocalDateTime.now());
			entity.setSessionId(sessionId);

			if (userId != null) {
				entity.setCreateUser(userId);
				entity.setUpdateUser(userId);
			}

			baseMapper.insert(entity);
		}
		catch (Exception ex) {
			log.error(
					"Failed to persist auth audit: eventType={}, result={}, userId={}, principal={}, sessionId={}, cause={}",
					eventType, result, userId, principal, sessionId, ex, ex);
		}
	}

}
