package com.auth.service.auth.infrastructure.audit.adapter;

import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.auth.application.port.login.LoginAuditWritePort;
import com.auth.service.auth.domain.authentication.model.LoginAuditSnapshot;
import com.auth.service.auth.domain.authentication.model.enums.AuthLoginLogResult;
import com.auth.service.auth.domain.authentication.model.enums.AuthLoginLogType;
import com.auth.service.auth.infrastructure.persistence.mapper.LoginLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 登录审计落库适配器：异步将认证审计写入 {@code log_login}。
 *
 * @author Bunny
 */
@Slf4j
@Repository
public class LoginAuditWriteAdapter extends ServiceImpl<LoginLogMapper, LoginLogEntity> implements LoginAuditWritePort {

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
