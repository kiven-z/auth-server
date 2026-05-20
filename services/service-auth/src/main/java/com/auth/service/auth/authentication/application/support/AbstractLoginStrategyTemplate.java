package com.auth.service.auth.authentication.application.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.event.LoginAuditEventPublisher;
import com.auth.service.auth.authentication.domain.model.AuthenticatedUser;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.application.port.LoginStrategy;
import com.auth.service.auth.authentication.infrastructure.token.LoginFailureRateLimiter;
import com.auth.service.auth.authorization.application.AuthProfileQueryService;
import com.auth.service.auth.shared.audit.AuthLoginAuditMapping;
import com.auth.service.auth.shared.exception.AuthBadCredentialsException;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录策略模板：解析用户 → 锁定检查 → 子类校验凭证 → 构建画像与响应；失败路径发布审计事件。
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

	/**
	 * 与 {@link UserEntity#getStatus()} 对齐：0=禁用
	 */
	private static final int SYS_USER_STATUS_DISABLED = 0;

	/**
	 * 与 {@link UserEntity#getStatus()} 对齐：2=锁定
	 */
	private static final int SYS_USER_STATUS_LOCKED = 2;

	protected final LoginFailureRateLimiter loginFailureRateLimiter;

	private final AuthProfileQueryService authProfileQueryService;

	private final LoginAuditEventPublisher loginAuditEventPublisher;

	protected AbstractLoginStrategyTemplate(LoginFailureRateLimiter loginFailureRateLimiter,
			AuthProfileQueryService authProfileQueryService, LoginAuditEventPublisher loginAuditEventPublisher) {
		this.loginFailureRateLimiter = loginFailureRateLimiter;
		this.authProfileQueryService = authProfileQueryService;
		this.loginAuditEventPublisher = loginAuditEventPublisher;
	}

	private static String principalForAudit(AbstractLoginCommand command) {
		String principal = command.principalForAudit();
		return CharSequenceUtil.isBlank(principal) ? null : principal;
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
			loginAuditEventPublisher.publish(request, loginLogType(), AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR,
					userId, user.getUsername(), "remainingAttempts=" + remaining, null);
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, remaining);
		}
		catch (AuthBusinessException ex) {
			// 按业务码映射发布凭证校验失败审计
			AuthResultCode code = ex.getResultCode();
			AuthLoginAuditMapping.credentialFailureAudit(code)
				.ifPresent(auditResult -> loginAuditEventPublisher.publish(request, loginLogType(), auditResult,
						user.getId(), user.getUsername(), ex.getMessageKey(), null));
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
				loginAuditEventPublisher.publish(request, loginLogType(),
						AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR, null, principalForAudit(command),
						ex.getMessageKey(), null);
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
			// 用户状态需要是正常状态
			if (rawStatus == null || rawStatus == SYS_USER_STATUS_DISABLED) {
				loginAuditEventPublisher.publish(request, loginLogType(), AuthLoginLogResult.ACCOUNT_DISABLED, userId,
						user.getUsername(), AuthResultCode.USER_DISABLED.getMessageKey(), null);
				throw new AuthBusinessException(AuthResultCode.USER_DISABLED);
			}
			if (rawStatus == SYS_USER_STATUS_LOCKED) {
				loginAuditEventPublisher.publish(request, loginLogType(), AuthLoginLogResult.ACCOUNT_LOCKED, userId,
						user.getUsername(), "account.status.locked", null);
				throw new AuthBusinessException(AuthResultCode.USER_LOCKED);
			}

			loginFailureRateLimiter.assertNotLocked(userId);
		}
		catch (AuthBusinessException ex) {
			// 按业务码映射发布账号锁定类审计
			AuthResultCode code = ex.getResultCode();
			AuthLoginAuditMapping.lockFailureAudit(code)
				.ifPresent(auditResult -> loginAuditEventPublisher.publish(request, loginLogType(), auditResult,
						user.getId(), user.getUsername(), ex.getMessageKey(), null));
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
