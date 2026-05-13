package com.auth.service.auth.authentication.audit;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.auth.support.model.enums.AuthLoginLogResult;
import com.auth.service.auth.support.model.enums.AuthLoginLogType;
import com.auth.service.auth.support.model.enums.AuthResultCode;
import com.auth.service.auth.support.model.request.login.AbstractLoginRequest;
import com.auth.service.auth.support.model.request.login.EmailLoginRequest;
import com.auth.service.auth.support.model.request.login.UsernamePasswordLoginRequest;
import lombok.experimental.UtilityClass;

/**
 * 登录审计辅助：从认证请求/用户实体解析 principal，以及编排失败时的结果码映射。
 *
 * @author Bunny
 */
@UtilityClass
public class LoginAuditContextResolver {

	/**
	 * 从认证请求体解析登录主体（用户名或邮箱），用于尚未解析到 UserEntity 时的审计。
	 * @param auth 认证请求
	 * @return 主体字符串，无法解析时返回 null
	 */
	public static String principal(AbstractLoginRequest auth) {
		if (auth instanceof UsernamePasswordLoginRequest usernamePasswordLoginRequest) {
			return blankToNull(usernamePasswordLoginRequest.getUsername());
		}
		if (auth instanceof EmailLoginRequest emailLoginRequest) {
			return blankToNull(emailLoginRequest.getEmail());
		}
		return null;
	}

	/**
	 * 根据请求类型推断审计事件类型（用于无匹配策略等编排期失败）。
	 * @param auth 认证请求
	 * @return 事件类型；未知类型时回退为密码登录类型
	 */
	public static AuthLoginLogType eventTypeFromAuth(AbstractLoginRequest auth) {
		if (auth instanceof EmailLoginRequest) {
			return AuthLoginLogType.LOGIN_EMAIL;
		}
		return AuthLoginLogType.LOGIN_PASSWORD;
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
