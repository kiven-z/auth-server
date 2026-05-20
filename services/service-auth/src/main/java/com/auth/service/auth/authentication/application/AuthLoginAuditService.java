package com.auth.service.auth.authentication.application;

import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import com.auth.service.auth.authentication.infrastructure.audit.LoginAuditSnapshotFactory;
import com.auth.service.auth.authentication.infrastructure.persistence.LoginLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录与安全审计持久化应用服务。
 *
 * @author Bunny
 */
@Slf4j
@Service
public class AuthLoginAuditService extends ServiceImpl<LoginLogMapper, LoginLogEntity> {

	private final LoginAuditSnapshotFactory loginAuditSnapshotFactory;

	public AuthLoginAuditService(LoginAuditSnapshotFactory loginAuditSnapshotFactory) {
		this.loginAuditSnapshotFactory = loginAuditSnapshotFactory;
	}

	/**
	 * 记录一条认证相关审计（登录、登出、刷新令牌等）。
	 * @param request HTTP 请求，用于 IP/UA；可为 null，此时地域与设备字段不落库
	 * @param eventType 事件类型
	 * @param result 结果码（与 {@code log_login.login_result} 一致）
	 * @param userId 用户 ID，未识别可为 null
	 * @param principal 用户名或邮箱等冗余检索字段
	 * @param failureReason 失败原因（messageKey 或简短说明）；成功时可为 null
	 * @param sessionId 会话 ID（如 jti）；无可为空
	 */
	public void recordLoginLog(HttpServletRequest request, AuthLoginLogType eventType, AuthLoginLogResult result,
			Long userId, String principal, String failureReason, String sessionId) {
		recordLoginLog(loginAuditSnapshotFactory.build(request), eventType, result, userId, principal, failureReason,
				sessionId);
	}

	/**
	 * 使用已解析的请求快照记录审计（供事件监听器等路径调用）。
	 * @param snapshot 请求快照，不可为 null；无请求信息时使用 {@link LoginAuditSnapshot#empty()}
	 * @param eventType 事件类型
	 * @param result 结果码
	 * @param userId 用户 ID
	 * @param principal 主体
	 * @param failureReason 失败原因
	 * @param sessionId 会话 ID
	 */
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
