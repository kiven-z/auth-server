package com.auth.service.auth.authentication.application.login.audit;

import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import com.auth.service.auth.authentication.domain.port.LoginAuditRecorder;
import com.auth.service.auth.authentication.domain.port.LoginAuditSnapshotPort;
import com.auth.service.auth.shared.audit.AuthLoginAuditMapping;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 *
 * 登录相关审计协调器：在请求线程解析快照并规范化主体，经 {@link LoginAuditRecorder} 异步落库。
 *
 * @author Bunny
 *
 */
@Component
public class LoginAuditCoordinator {

	private final LoginAuditSnapshotPort loginAuditSnapshotPort;

	private final LoginAuditRecorder loginAuditRecorder;

	private final LoginAuditPrincipalNormalizer loginAuditPrincipalNormalizer;

	public LoginAuditCoordinator(LoginAuditSnapshotPort loginAuditSnapshotPort, LoginAuditRecorder loginAuditRecorder,
			LoginAuditPrincipalNormalizer loginAuditPrincipalNormalizer) {
		this.loginAuditSnapshotPort = loginAuditSnapshotPort;
		this.loginAuditRecorder = loginAuditRecorder;
		this.loginAuditPrincipalNormalizer = loginAuditPrincipalNormalizer;
	}

	/**
	 * 不支持的登录命令类型（策略表未命中）。
	 * @param request HTTP 请求
	 * @param command 登录命令
	 * @param messageKey 失败原因 messageKey
	 */
	public void auditUnsupportedLoginCommand(HttpServletRequest request, AbstractLoginCommand command,
			String messageKey) {
		publishNormalized(request, command.loginLogType(), AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, null,
				LoginAuditContextResolver.principal(command), messageKey, null);
	}

	/**
	 * 解析用户失败且为「用户名或密码错误」类（不暴露用户是否存在）。
	 * @param request HTTP 请求
	 * @param logType 登录日志类型
	 * @param principal 审计主体
	 * @param messageKey 失败原因 messageKey
	 */
	public void auditUserUnknownAfterResolve(HttpServletRequest request, AuthLoginLogType logType, String principal,
			String messageKey) {
		publishNormalized(request, logType, AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, null, principal,
				messageKey, null);
	}

	/**
	 * 账户禁用（状态位）。
	 * @param request HTTP 请求
	 * @param logType 登录日志类型
	 * @param userId 用户 ID
	 * @param username 用户名
	 * @param messageKey 失败原因 messageKey
	 */
	public void auditAccountDisabled(HttpServletRequest request, AuthLoginLogType logType, Long userId, String username,
			String messageKey) {
		publishNormalized(request, logType, AuthLoginLogResult.ACCOUNT_DISABLED, userId, username, messageKey, null);
	}

	/**
	 * 账户状态为锁定（状态位）。
	 * @param request HTTP 请求
	 * @param logType 登录日志类型
	 * @param userId 用户 ID
	 * @param username 用户名
	 */
	public void auditAccountLockedByStatus(HttpServletRequest request, AuthLoginLogType logType, Long userId,
			String username) {
		publishNormalized(request, logType, AuthLoginLogResult.ACCOUNT_LOCKED, userId, username,
				"account.status.locked", null);
	}

	/**
	 * 凭证错误且带剩余尝试次数说明。
	 * @param request HTTP 请求
	 * @param logType 登录日志类型
	 * @param userId 用户 ID
	 * @param username 用户名
	 * @param remainingAttempts 剩余尝试次数
	 */
	public void auditBadCredentialsWithRemaining(HttpServletRequest request, AuthLoginLogType logType, Long userId,
			String username, int remainingAttempts) {
		publishNormalized(request, logType, AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, userId, username,
				"remainingAttempts=" + remainingAttempts, null);
	}

	/**
	 * 按业务码映射发布「凭证类」失败审计（可选映射）。
	 * @param request HTTP 请求
	 * @param logType 登录日志类型
	 * @param userId 用户 ID
	 * @param username 用户名
	 * @param ex 业务异常
	 */
	public void auditMappedCredentialFailure(HttpServletRequest request, AuthLoginLogType logType, Long userId,
			String username, AuthBusinessException ex) {
		AuthLoginAuditMapping.credentialFailureAudit(ex.getResultCode())
			.ifPresent(
					result -> publishNormalized(request, logType, result, userId, username, ex.getMessageKey(), null));
	}

	/**
	 * 按业务码映射发布「锁定类」失败审计（可选映射）。
	 * @param request HTTP 请求
	 * @param logType 登录日志类型
	 * @param userId 用户 ID
	 * @param username 用户名
	 * @param ex 业务异常
	 */
	public void auditMappedLockFailure(HttpServletRequest request, AuthLoginLogType logType, Long userId,
			String username, AuthBusinessException ex) {
		AuthLoginAuditMapping.lockFailureAudit(ex.getResultCode())
			.ifPresent(
					result -> publishNormalized(request, logType, result, userId, username, ex.getMessageKey(), null));
	}

	/**
	 * 登录编排成功（已签发令牌并建立会话后）。
	 * @param request HTTP 请求
	 * @param logType 登录日志类型
	 * @param userId 用户 ID
	 * @param username 用户名
	 * @param jti 会话 ID
	 */
	public void auditLoginSuccess(HttpServletRequest request, AuthLoginLogType logType, Long userId, String username,
			String jti) {
		publishNormalized(request, logType, AuthLoginLogResult.SUCCESS, userId, username, null, jti);
	}

	/**
	 * 登录编排阶段失败（令牌/会话写入失败等）。
	 * @param request HTTP 请求
	 * @param logType 登录日志类型
	 * @param userId 用户 ID
	 * @param username 用户名
	 * @param ex 业务异常
	 */
	public void auditOrchestrationFailure(HttpServletRequest request, AuthLoginLogType logType, Long userId,
			String username, AuthBusinessException ex) {
		AuthLoginLogResult result = LoginAuditContextResolver.resultForOrchestrationFailure(ex.getResultCode());
		publishNormalized(request, logType, result, userId, username, ex.getMessageKey(), null);
	}

	/**
	 * 登出成功审计（编排层在撤销会话后单点调用）。
	 * @param request HTTP 请求
	 * @param userId 用户 ID
	 * @param jti 会话 ID
	 */
	public void auditLogoutSuccess(HttpServletRequest request, Long userId, String jti) {
		publishNormalized(request, AuthLoginLogType.LOGOUT, AuthLoginLogResult.SUCCESS, userId, null, null, jti);
	}

	/**
	 * 刷新令牌失败审计。
	 * @param request HTTP 请求
	 * @param userId 用户 ID（可能为 null）
	 * @param ex 业务异常
	 */
	public void auditRefreshTokenFailure(HttpServletRequest request, Long userId, AuthBusinessException ex) {
		publishNormalized(request, AuthLoginLogType.REFRESH_TOKEN, AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR,
				userId, null, ex.getMessageKey(), null);
	}

	/**
	 * 刷新令牌成功审计。
	 * @param request HTTP 请求
	 * @param userId 用户 ID
	 * @param jti 会话 ID
	 */
	public void auditRefreshTokenSuccess(HttpServletRequest request, Long userId, String jti) {
		publishNormalized(request, AuthLoginLogType.REFRESH_TOKEN, AuthLoginLogResult.SUCCESS, userId, null, null, jti);
	}

	private void publishNormalized(HttpServletRequest request, AuthLoginLogType eventType, AuthLoginLogResult result,
			Long userId, String principal, String failureReason, String sessionId) {
		LoginAuditSnapshot snapshot = loginAuditSnapshotPort.build(request);
		String normalizedPrincipal = loginAuditPrincipalNormalizer.normalize(userId, principal);
		loginAuditRecorder.recordLoginLog(snapshot, eventType, result, userId, normalizedPrincipal, failureReason,
				sessionId);
	}

}
