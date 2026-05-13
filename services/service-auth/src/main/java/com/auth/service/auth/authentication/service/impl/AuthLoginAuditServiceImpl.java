package com.auth.service.auth.authentication.service.impl;

import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.auth.authentication.audit.LoginAuditSnapshot;
import com.auth.service.auth.authentication.audit.LoginAuditSnapshotFactory;
import com.auth.service.auth.authentication.mapper.LoginLogMapper;
import com.auth.service.auth.authentication.service.AuthLoginAuditService;
import com.auth.service.auth.support.model.enums.AuthLoginLogResult;
import com.auth.service.auth.support.model.enums.AuthLoginLogType;
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

	private final LoginAuditSnapshotFactory loginAuditSnapshotFactory;

	public AuthLoginAuditServiceImpl(LoginAuditSnapshotFactory loginAuditSnapshotFactory) {
		this.loginAuditSnapshotFactory = loginAuditSnapshotFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordLoginLog(HttpServletRequest request, AuthLoginLogType eventType, AuthLoginLogResult result,
			Long userId, String principal, String failureReason, String sessionId) {
		recordLoginLog(loginAuditSnapshotFactory.build(request), eventType, result, userId, principal, failureReason,
				sessionId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
