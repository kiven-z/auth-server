package com.auth.service.auth.authentication.application.support;

import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import lombok.experimental.UtilityClass;

/**
 * 登录审计辅助：解析审计主体、编排失败时的结果码映射。
 *
 * @author Bunny
 */
@UtilityClass
public class LoginAuditContextResolver {

	/**
	 * 从登录命令解析审计主体。
	 * @param command 登录命令
	 * @return 主体字符串，无法解析时返回 null
	 */
	public static String principal(AbstractLoginCommand command) {
		if (command == null) {
			return null;
		}
		String auditPrincipal = command.principalForAudit();
		return auditPrincipal == null || auditPrincipal.isBlank() ? null : auditPrincipal;
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

}
