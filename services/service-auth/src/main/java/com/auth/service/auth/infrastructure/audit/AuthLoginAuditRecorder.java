package com.auth.service.auth.infrastructure.audit;

import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.auth.application.login.port.LoginAuditRecorder;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogResult;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.model.LoginAuditSnapshot;
import com.auth.service.auth.infrastructure.persistence.LoginLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * {@link LoginAuditRecorder} 实现：异步将认证审计写入 {@code log_login}。
 *
 * @author Bunny
 */
@Slf4j
@Service
public class AuthLoginAuditRecorder extends ServiceImpl<LoginLogMapper, LoginLogEntity> implements LoginAuditRecorder {

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
