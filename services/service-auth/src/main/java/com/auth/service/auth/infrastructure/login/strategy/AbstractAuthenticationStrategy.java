package com.auth.service.auth.infrastructure.login.strategy;

import cn.hutool.core.bean.BeanUtil;
import com.auth.module.platform.persistence.UserEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.exception.BadCredentialsException;
import com.auth.service.auth.infrastructure.login.LoginAuditSupport;
import com.auth.service.auth.infrastructure.login.LoginFailureRateLimiter;
import com.auth.service.auth.infrastructure.login.LoginResult;
import com.auth.service.auth.infrastructure.profile.AuthProfileService;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.request.AbstractAuthentication;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.service.AuthLoginAuditService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证模板：解析用户 → 锁定检查 → 子类校验凭证 → 构建画像与响应；失败路径写入审计日志。
 *
 * <p>
 * 会话容量由 {@link com.auth.service.auth.infrastructure.session.SessionLimitManager}
 * 在编排层统一处理。
 * </p>
 *
 * @author Bunny
 */
public abstract class AbstractAuthenticationStrategy<T extends AbstractAuthentication> implements LoginStrategy<T> {

	/**
	 * 与 {@link UserEntity#getStatus()} 对齐：0=禁用
	 */
	private static final int SYS_USER_STATUS_DISABLED = 0;

	/**
	 * 与 {@link UserEntity#getStatus()} 对齐：2=锁定
	 */
	private static final int SYS_USER_STATUS_LOCKED = 2;

	protected final LoginFailureRateLimiter loginFailureRateLimiter;

	private final AuthProfileService authProfileService;

	private final AuthLoginAuditService loginLogService;

	protected AbstractAuthenticationStrategy(LoginFailureRateLimiter loginFailureRateLimiter,
			AuthProfileService authProfileService, AuthLoginAuditService loginLogService) {
		this.loginFailureRateLimiter = loginFailureRateLimiter;
		this.authProfileService = authProfileService;
		this.loginLogService = loginLogService;
	}

	/**
	 * 当前策略对应的审计事件类型（用于失败时尚未执行 {@link #doAuthenticate} 的场景）。
	 * @return 登录日志类型
	 */
	protected abstract AuthLoginLogType loginLogType();

	@Override
	public LoginResult authenticate(T auth, HttpServletRequest request) {
		UserEntity user = resolveUserOrAuditAndRethrow(auth, request);
		Long userId = user.getId();
		assertNotLockedOrAuditAndRethrow(request, user);

		try {
			AuthLoginLogType resolvedLoginLogType = doAuthenticate(auth, user);
			AuthProfile profile = authProfileService.buildByUser(user);

			UserLoginResponse response = BeanUtil.copyProperties(user, UserLoginResponse.class);
			response.setRoles(profile.getRoles());
			response.setPermissions(profile.getPermissions());

			return new LoginResult(response, profile, resolvedLoginLogType);
		}
		catch (BadCredentialsException ex) {
			int remaining = loginFailureRateLimiter.recordFailure(userId);
			loginLogService.recordLoginLog(request, loginLogType(), AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR,
					userId, user.getUsername(), "remainingAttempts=" + remaining, null);
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR_WITH_REMAINING, remaining);
		}
		catch (AuthBusinessException ex) {
			AuthResultCode code = ex.getResultCode();
			if (code == AuthResultCode.AUTH_CODE_ERROR) {
				loginLogService.recordLoginLog(request, loginLogType(), AuthLoginLogResult.CAPTCHA_ERROR, userId,
						user.getUsername(), ex.getMessageKey(), null);
			}
			if (code == AuthResultCode.USER_DISABLED) {
				loginLogService.recordLoginLog(request, loginLogType(), AuthLoginLogResult.ACCOUNT_DISABLED, userId,
						user.getUsername(), ex.getMessageKey(), null);
			}
			throw ex;
		}
	}

	/**
	 * 解析用户实体并记录审计日志。
	 * @param auth 登录请求
	 * @param request HTTP 请求
	 * @return 用户实体
	 */
	private UserEntity resolveUserOrAuditAndRethrow(T auth, HttpServletRequest request) {
		try {
			return resolveUser(auth);
		}
		catch (AuthBusinessException ex) {
			if (ex.getResultCode() == AuthResultCode.USERNAME_OR_PASSWORD_ERROR) {
				loginLogService.recordLoginLog(request, loginLogType(), AuthLoginLogResult.PASSWORD_OR_CREDENTIAL_ERROR,
						null, LoginAuditSupport.principal(auth), ex.getMessageKey(), null);
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
			if (rawStatus == null || rawStatus == SYS_USER_STATUS_DISABLED) {
				loginLogService.recordLoginLog(request, loginLogType(), AuthLoginLogResult.ACCOUNT_DISABLED, userId,
						user.getUsername(), AuthResultCode.USER_DISABLED.getMessageKey(), null);
				throw new AuthBusinessException(AuthResultCode.USER_DISABLED);
			}
			if (rawStatus == SYS_USER_STATUS_LOCKED) {
				loginLogService.recordLoginLog(request, loginLogType(), AuthLoginLogResult.ACCOUNT_LOCKED, userId,
						user.getUsername(), "account.status.locked", null);
				throw new AuthBusinessException(AuthResultCode.USER_LOCKED);
			}

			loginFailureRateLimiter.assertNotLocked(userId);
		}
		catch (AuthBusinessException ex) {
			// 判断是否为锁定结果代码
			AuthResultCode code = ex.getResultCode();
			boolean codeFlag = code == AuthResultCode.USER_LOCKED_WITH_MINUTES || code == AuthResultCode.USER_LOCKED;
			if (codeFlag) {
				loginLogService.recordLoginLog(request, loginLogType(), AuthLoginLogResult.ACCOUNT_LOCKED, userId,
						user.getUsername(), ex.getMessageKey(), null);
			}
			throw ex;
		}
	}

	/**
	 * 解析认证请求对应的用户实体。
	 * @param auth 登录请求
	 * @return 用户实体
	 */
	protected abstract UserEntity resolveUser(T auth);

	/**
	 * 校验凭证；密码不匹配时抛出 {@link BadCredentialsException}。
	 * @param auth 登录请求
	 * @param user 已解析用户
	 * @return 登录类型（与审计类型一致）
	 */
	protected abstract AuthLoginLogType doAuthenticate(T auth, UserEntity user);

}
