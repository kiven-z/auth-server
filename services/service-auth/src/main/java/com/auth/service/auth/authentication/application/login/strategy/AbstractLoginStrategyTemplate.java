package com.auth.service.auth.authentication.application.login.strategy;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditContextResolver;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
import com.auth.service.auth.authentication.application.port.LoginStrategy;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.AuthenticatedUser;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.port.LoginFailureRateLimitPort;
import com.auth.service.auth.authentication.domain.service.UserAccountLoginEligibility;
import com.auth.service.auth.authorization.domain.port.AuthProfileQueryPort;
import com.auth.service.auth.shared.exception.AuthBadCredentialsException;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录策略模板：解析用户 → 锁定检查 → 子类校验凭证 → 构建画像与返回；失败路径经 {@link LoginAuditCoordinator} 发布审计。
 *
 * <p>
 * 会话容量由 {@link com.auth.service.auth.authentication.application.port.SessionLimitPort} 在
 * {@link com.auth.service.auth.authentication.application.LoginApplicationService}
 * 编排层统一处理。
 * </p>
 *
 * @author Bunny
 */
public abstract class AbstractLoginStrategyTemplate<T extends AbstractLoginCommand> implements LoginStrategy<T> {

	protected final LoginFailureRateLimitPort loginFailureRateLimitPort;

	private final AuthProfileQueryPort authProfileQueryPort;

	private final LoginAuditCoordinator loginAuditCoordinator;

	protected AbstractLoginStrategyTemplate(LoginFailureRateLimitPort loginFailureRateLimitPort,
			AuthProfileQueryPort authProfileQueryPort, LoginAuditCoordinator loginAuditCoordinator) {
		this.loginFailureRateLimitPort = loginFailureRateLimitPort;
		this.authProfileQueryPort = authProfileQueryPort;
		this.loginAuditCoordinator = loginAuditCoordinator;
	}

	/**
	 * 当前策略对应的审计事件类型（用于失败时尚未执行 {@link #doAuthenticate} 的场景）。
	 * @return 登录日志类型
	 */
	protected abstract AuthLoginLogType loginLogType();

	@Override
	public LoginResult authenticate(T command, HttpServletRequest request) {
		LoginAccount account = resolveAccountOrAuditAndRethrow(command, request);
		Long userId = account.id();
		assertNotLockedOrAuditAndRethrow(request, account);

		try {
			AuthLoginLogType resolvedLoginLogType = doAuthenticate(command, account);
			AuthProfile profile = authProfileQueryPort.buildByUserId(userId);

			return new LoginResult(AuthenticatedUser.from(account), profile, resolvedLoginLogType);
		}
		catch (AuthBadCredentialsException ex) {
			int remaining = loginFailureRateLimitPort.recordFailure(userId);
			loginAuditCoordinator.auditBadCredentialsWithRemaining(request, loginLogType(), userId, account.username(),
					remaining);
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, remaining);
		}
		catch (AuthBusinessException ex) {
			loginAuditCoordinator.auditMappedCredentialFailure(request, loginLogType(), account.id(),
					account.username(), ex);
			throw ex;
		}
	}

	/**
	 * 解析登录账户并记录审计日志。
	 * @param command 登录命令
	 * @param request HTTP 请求
	 * @return 登录账户
	 */
	private LoginAccount resolveAccountOrAuditAndRethrow(T command, HttpServletRequest request) {
		try {
			return resolveAccount(command);
		}
		catch (AuthBusinessException ex) {
			if (ex.getResultCode() == AuthResultCode.USERNAME_OR_PASSWORD_ERROR) {
				loginAuditCoordinator.auditUserUnknownAfterResolve(request, loginLogType(),
						LoginAuditContextResolver.principal(command), ex.getMessageKey());
			}
			throw ex;
		}
	}

	/**
	 * 校验用户是否锁定并记录审计日志。
	 * @param request HTTP 请求
	 * @param account 登录账户
	 */
	private void assertNotLockedOrAuditAndRethrow(HttpServletRequest request, LoginAccount account) {
		Long userId = account.id();
		try {
			UserAccountLoginEligibility eligibility = UserAccountLoginEligibility.fromStatus(account.status());
			switch (eligibility) {
				case DISABLED -> {
					loginAuditCoordinator.auditAccountDisabled(request, loginLogType(), userId, account.username(),
							AuthResultCode.USER_DISABLED.getMessageKey());
					throw new AuthBusinessException(AuthResultCode.USER_DISABLED);
				}
				case STATUS_LOCKED -> {
					loginAuditCoordinator.auditAccountLockedByStatus(request, loginLogType(), userId,
							account.username());
					throw new AuthBusinessException(AuthResultCode.USER_LOCKED);
				}
				case ALLOWED -> {
					// 继续检查登录失败锁定计数
				}
			}

			loginFailureRateLimitPort.assertNotLocked(userId);
		}
		catch (AuthBusinessException ex) {
			loginAuditCoordinator.auditMappedLockFailure(request, loginLogType(), account.id(), account.username(), ex);
			throw ex;
		}
	}

	/**
	 * 解析认证命令对应的登录账户。
	 * @param command 登录命令
	 * @return 登录账户
	 */
	protected abstract LoginAccount resolveAccount(T command);

	/**
	 * 校验凭证；密码不匹配时抛出 {@link AuthBadCredentialsException}。
	 * @param command 登录命令
	 * @param account 已解析账户
	 * @return 登录类型（与审计类型一致）
	 */
	protected abstract AuthLoginLogType doAuthenticate(T command, LoginAccount account);

}
