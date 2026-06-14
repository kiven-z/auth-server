package com.auth.service.auth.application.login.audit;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.application.login.input.credential.AbstractCredentialLogin;
import com.auth.service.auth.application.login.port.AuthProfileCachePort;
import com.auth.service.auth.application.login.port.LoginAuditRecorder;
import com.auth.service.auth.application.login.port.LoginAuditSnapshotPort;
import com.auth.service.auth.application.port.UserReadPort;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogResult;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.model.LoginAuditSnapshot;
import com.auth.service.auth.domain.authorization.model.UserAccountSnapshot;
import com.auth.service.auth.shared.audit.AuthLoginAuditMapping;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * 登录审计应用服务：解析请求快照、规范化主体并委托 {@link LoginAuditRecorder} 落库。
 *
 * @author Bunny
 */
@Service
public class LoginAuditService {

	/**
	 * 历史占位主体，写入前需替换为真实登录名。
	 */
	private static final String LEGACY_PLACEHOLDER = "/";

	private final LoginAuditSnapshotPort loginAuditSnapshotPort;

	private final LoginAuditRecorder loginAuditRecorder;

	private final AuthProfileCachePort authProfileCachePort;

	private final UserReadPort userReadPort;

	public LoginAuditService(LoginAuditSnapshotPort loginAuditSnapshotPort, LoginAuditRecorder loginAuditRecorder,
			AuthProfileCachePort authProfileCachePort, UserReadPort userReadPort) {
		this.loginAuditSnapshotPort = loginAuditSnapshotPort;
		this.loginAuditRecorder = loginAuditRecorder;
		this.authProfileCachePort = authProfileCachePort;
		this.userReadPort = userReadPort;
	}

	private static String blankToNull(String value) {
		return CharSequenceUtil.isBlank(value) ? null : value.trim();
	}

	/**
	 * 将业务结果码映射为审计结果枚举（编排阶段失败等）。
	 * @param code 业务结果码
	 * @return 审计结果
	 */
	private static AuthLoginLogResult resultForOrchestrationFailure(AuthResultCode code) {
		if (code == null) {
			return AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR;
		}
		return switch (code) {
			case USER_LOCKED, USER_LOCKED_WITH_MINUTES -> AuthLoginLogResult.ACCOUNT_LOCKED;
			case AUTH_CODE_ERROR -> AuthLoginLogResult.CAPTCHA_ERROR;
			case USER_DISABLED -> AuthLoginLogResult.ACCOUNT_DISABLED;
			default -> AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR;
		};
	}

	/**
	 * 从登录命令解析审计主体（手机号/邮箱/输入用户名等）。
	 * @param credential 登录命令
	 * @return 主体字符串，无法解析时返回 null
	 */
	public String resolvePrincipal(AbstractCredentialLogin credential) {
		if (credential == null) {
			return null;
		}
		String auditPrincipal = credential.principalForAudit();
		return auditPrincipal == null || auditPrincipal.isBlank() ? null : auditPrincipal;
	}

	/**
	 * 不支持的登录命令类型（策略表未命中）。
	 * @param request HTTP 请求
	 * @param credential 登录命令
	 * @param messageKey 失败原因 messageKey
	 */
	public void auditUnsupportedLoginCommand(HttpServletRequest request, AbstractCredentialLogin credential,
			String messageKey) {
		publishNormalized(request, credential.loginLogType(), AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, null,
				resolvePrincipal(credential), messageKey, null);
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
		AuthLoginLogResult result = resultForOrchestrationFailure(ex.getResultCode());
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
		String normalizedPrincipal = normalizePrincipal(userId, principal);
		loginAuditRecorder.recordLoginLog(snapshot, eventType, result, userId, normalizedPrincipal, failureReason,
				sessionId);
	}

	/**
	 * 规范化审计主体：有 userId 时优先落库系统登录名，无 userId 时保留登录尝试主体。
	 * @param userId 用户 ID
	 * @param incomingPrincipal 调用方传入的主体
	 * @return 规范化后的主体
	 */
	private String normalizePrincipal(Long userId, String incomingPrincipal) {
		if (userId == null) {
			return blankToNull(incomingPrincipal);
		}

		String trimmed = blankToNull(incomingPrincipal);
		boolean isUsableAccountPrincipal = CharSequenceUtil.isNotBlank(trimmed) && !LEGACY_PLACEHOLDER.equals(trimmed);
		if (isUsableAccountPrincipal) {
			return trimmed;
		}

		return authProfileCachePort.loadCachedProfile(userId)
			.map(AuthProfile::getUsername)
			.filter(CharSequenceUtil::isNotBlank)
			.or(() -> userReadPort.findById(userId).map(UserAccountSnapshot::username))
			.orElse(null);
	}

}
