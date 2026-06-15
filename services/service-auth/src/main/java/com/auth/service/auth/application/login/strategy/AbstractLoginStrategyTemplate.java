package com.auth.service.auth.application.login.strategy;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.application.login.audit.LoginAuditService;
import com.auth.service.auth.application.login.input.credential.AbstractCredentialLogin;
import com.auth.service.auth.application.port.login.LoginStrategy;
import com.auth.service.auth.application.port.AuthProfileQueryPort;
import com.auth.service.auth.application.port.CredentialGateway;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.model.AuthenticatedUser;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.domain.authentication.model.LoginResult;
import com.auth.service.auth.domain.authentication.service.UserAccountLoginEligibility;
import com.auth.service.auth.shared.exception.AuthBadCredentialsException;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录策略模板：解析用户 → 锁定检查 → 子类校验凭证 → 构建画像与返回；失败路径经 {@link LoginAuditService} 发布审计。
 *
 * @author Bunny
 */
public abstract class AbstractLoginStrategyTemplate<T extends AbstractCredentialLogin> implements LoginStrategy<T> {

	protected final CredentialGateway credentialGateway;

	private final AuthProfileQueryPort authProfileQueryPort;

	private final LoginAuditService loginAuditService;

	protected AbstractLoginStrategyTemplate(CredentialGateway credentialGateway,
			AuthProfileQueryPort authProfileQueryPort, LoginAuditService loginAuditService) {
		this.credentialGateway = credentialGateway;
		this.authProfileQueryPort = authProfileQueryPort;
		this.loginAuditService = loginAuditService;
	}

	/**
	 * 当前策略对应的审计事件类型（用于失败时尚未执行 {@link #doAuthenticate} 的场景）。
	 * @return 登录日志类型
	 */
	protected abstract AuthLoginLogType loginLogType();

	@Override
	public LoginResult authenticate(T command, HttpServletRequest request) {
		// 解析登录账户并记录审计日志
		LoginAccount account = resolveAccountOrAuditAndRethrow(command, request);
		Long userId = account.id();

		// 校验用户是否锁定并记录审计日志
		assertNotLockedOrAuditAndRethrow(request, account);

		try {
			// 校验凭证并记录审计日志
			AuthLoginLogType resolvedLoginLogType = doAuthenticate(command, account);
			// 构建认证用户画像
			AuthProfile profile = authProfileQueryPort.buildByUserId(userId);
			return new LoginResult(AuthenticatedUser.from(account), profile, resolvedLoginLogType);
		}
		// 凭证不匹配时记录审计日志
		catch (AuthBadCredentialsException ex) {
			int remaining = credentialGateway.recordFailure(userId);
			loginAuditService.auditBadCredentialsWithRemaining(request, loginLogType(), userId, account.username(),
					remaining);
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, remaining);
		}
		catch (AuthBusinessException ex) {
			loginAuditService.auditMappedCredentialFailure(request, loginLogType(), account.id(), account.username(),
					ex);
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
				loginAuditService.auditUserUnknownAfterResolve(request, loginLogType(),
						loginAuditService.resolvePrincipal(command), ex.getMessageKey());
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
					loginAuditService.auditAccountDisabled(request, loginLogType(), userId, account.username(),
							AuthResultCode.USER_DISABLED.getMessageKey());
					throw new AuthBusinessException(AuthResultCode.USER_DISABLED);
				}
				case STATUS_LOCKED -> {
					loginAuditService.auditAccountLockedByStatus(request, loginLogType(), userId, account.username());
					throw new AuthBusinessException(AuthResultCode.USER_LOCKED);
				}
				case ALLOWED -> {
					// 继续检查登录失败锁定计数
				}
				default -> throw new AuthBusinessException(AuthResultCode.AUTH_REQUEST_TYPE_UNSUPPORTED);
			}

			credentialGateway.assertNotLocked(userId);
		}
		catch (AuthBusinessException ex) {
			loginAuditService.auditMappedLockFailure(request, loginLogType(), account.id(), account.username(), ex);
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
