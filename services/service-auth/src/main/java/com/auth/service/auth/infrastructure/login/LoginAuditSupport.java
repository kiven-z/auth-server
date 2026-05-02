package com.auth.service.auth.infrastructure.login;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.starter.model.UserEntity;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.request.AbstractAuthentication;
import com.auth.service.auth.model.request.EmailAuth;
import com.auth.service.auth.model.request.UsernamePasswordAuth;
import lombok.experimental.UtilityClass;

/**
 * 登录审计辅助：从认证请求/用户实体解析 principal，以及编排失败时的结果码映射。
 *
 * @author Bunny
 */
@UtilityClass
public class LoginAuditSupport {

	/**
	 * 从认证请求体解析登录主体（用户名或邮箱），用于尚未解析到 UserEntity 时的审计。
	 * @param auth 认证请求
	 * @return 主体字符串，无法解析时返回 null
	 */
	public static String principal(AbstractAuthentication auth) {
		if (auth instanceof UsernamePasswordAuth usernamePasswordAuth) {
			return blankToNull(usernamePasswordAuth.getUsername());
		}
		if (auth instanceof EmailAuth emailAuth) {
			return blankToNull(emailAuth.getEmail());
		}
		return null;
	}

	/**
	 * 根据请求类型推断审计事件类型（用于无匹配策略等编排期失败）。
	 * @param auth 认证请求
	 * @return 事件类型；未知类型时回退为密码登录类型
	 */
	public static AuthLoginLogType eventTypeFromAuth(AbstractAuthentication auth) {
		if (auth instanceof EmailAuth) {
			return AuthLoginLogType.LOGIN_EMAIL;
		}
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	/**
	 * 从用户实体解析冗余展示名：优先用户名，其次邮箱。
	 * @param user 用户实体
	 * @return 主体字符串
	 */
	public static String principalFromUser(UserEntity user) {
		if (user == null) {
			return null;
		}
		if (CharSequenceUtil.isNotBlank(user.getUsername())) {
			return user.getUsername();
		}
		return blankToNull(user.getEmail());
	}

	/**
	 * 将业务结果码映射为审计结果枚举（编排阶段失败等）。
	 * @param code 业务结果码
	 * @return 审计结果
	 */
	public static AuthLoginLogResult resultForOrchestrationFailure(AuthResultCode code) {
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

	private static String blankToNull(String value) {
		return CharSequenceUtil.isBlank(value) ? null : value;
	}

}
