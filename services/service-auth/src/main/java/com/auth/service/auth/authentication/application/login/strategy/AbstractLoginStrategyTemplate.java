package com.auth.service.auth.authentication.application.login.strategy;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditContextResolver;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditCoordinator;
import com.auth.service.auth.authentication.application.port.LoginStrategy;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.AuthenticatedUser;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.service.UserAccountLoginEligibility;
import com.auth.service.auth.authentication.infrastructure.token.LoginFailureRateLimiter;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.shared.exception.AuthBadCredentialsException;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录策略模板：解析用户 → 锁定检查 → 子类校验凭证 → 构建画像与返回；失败路径经 {@link LoginAuditCoordinator} 发布审计。
 *
 * <p>
 * 会话容量由
 * {@link com.auth.service.auth.session.infrastructure.redis.support.SessionLimitManager}
 * 在 {@link com.auth.service.auth.authentication.application.LoginApplicationService}
 * 编排层统一处理。
 * </p>
 *
 * @author Bunny
 */
public abstract class AbstractLoginStrategyTemplate<T extends AbstractLoginCommand> implements LoginStrategy<T> {

	protected final LoginFailureRateLimiter loginFailureRateLimiter;

	private final AuthProfileQueryService authProfileQueryService;

	private final LoginAuditCoordinator loginAuditCoordinator;

	protected AbstractLoginStrategyTemplate(LoginFailureRateLimiter loginFailureRateLimiter,
			AuthProfileQueryService authProfileQueryService, LoginAuditCoordinator loginAuditCoordinator) {
		this.loginFailureRateLimiter = loginFailureRateLimiter;
		this.authProfileQueryService = authProfileQueryService;
		this.loginAuditCoordinator = loginAuditCoordinator;
	}

	/**
	 * 当前策略对应的审计事件类型（用于失败时尚未执行 {@link #doAuthenticate} 的场景）。
	 * @return 登录日志类型
	 */
	protected abstract AuthLoginLogType loginLogType();

	@Override
	public LoginResult authenticate(T command, HttpServletRequest request) {
		UserEntity user = resolveUserOrAuditAndRethrow(command, request);
		Long userId = user.getId();
		// 校验当前登录用户是否超出最大上限
		assertNotLockedOrAuditAndRethrow(request, user);

		try {
			// 构建用户画像并返回
			AuthLoginLogType resolvedLoginLogType = doAuthenticate(command, user);
			AuthProfile profile = authProfileQueryService.buildByUser(user);

			return new LoginResult(AuthenticatedUser.from(user), profile, resolvedLoginLogType);
		}
		catch (AuthBadCredentialsException ex) {
			int remaining = loginFailureRateLimiter.recordFailure(userId);
			loginAuditCoordinator.auditBadCredentialsWithRemaining(request, loginLogType(), userId, user.getUsername(),
					remaining);
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, remaining);
		}
		catch (AuthBusinessException ex) {
			loginAuditCoordinator.auditMappedCredentialFailure(request, loginLogType(), user.getId(),
					user.getUsername(), ex);
			throw ex;
		}
	}

	/**
	 * 解析用户实体并记录审计日志。
	 * @param command 登录命令
	 * @param request HTTP 请求
	 * @return 用户实体
	 */
	private UserEntity resolveUserOrAuditAndRethrow(T command, HttpServletRequest request) {
		try {
			return resolveUser(command);
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
	 * @param user 用户实体
	 */
	private void assertNotLockedOrAuditAndRethrow(HttpServletRequest request, UserEntity user) {
		Long userId = user.getId();
		Integer rawStatus = user.getStatus();
		try {
			UserAccountLoginEligibility eligibility = UserAccountLoginEligibility.fromStatus(rawStatus);
			switch (eligibility) {
				case DISABLED -> {
					loginAuditCoordinator.auditAccountDisabled(request, loginLogType(), userId, user.getUsername(),
							AuthResultCode.USER_DISABLED.getMessageKey());
					throw new AuthBusinessException(AuthResultCode.USER_DISABLED);
				}
				case STATUS_LOCKED -> {
					loginAuditCoordinator.auditAccountLockedByStatus(request, loginLogType(), userId,
							user.getUsername());
					throw new AuthBusinessException(AuthResultCode.USER_LOCKED);
				}
				case ALLOWED -> {
					// 继续检查登录失败锁定计数
				}
			}

			loginFailureRateLimiter.assertNotLocked(userId);
		}
		catch (AuthBusinessException ex) {
			loginAuditCoordinator.auditMappedLockFailure(request, loginLogType(), user.getId(), user.getUsername(), ex);
			throw ex;
		}
	}

	/**
	 * 解析认证命令对应的用户实体。
	 * @param command 登录命令
	 * @return 用户实体
	 */
	protected abstract UserEntity resolveUser(T command);

	/**
	 * 校验凭证；密码不匹配时抛出 {@link AuthBadCredentialsException}。
	 * @param command 登录命令
	 * @param user 已解析用户
	 * @return 登录类型（与审计类型一致）
	 */
	protected abstract AuthLoginLogType doAuthenticate(T command, UserEntity user);

}
