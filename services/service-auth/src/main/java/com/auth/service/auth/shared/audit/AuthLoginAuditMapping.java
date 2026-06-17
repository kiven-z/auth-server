package com.auth.service.auth.shared.audit;

import com.auth.service.auth.domain.authentication.model.enums.AuthLoginLogResult;
import com.auth.service.auth.shared.exception.AuthResultCode;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Optional;

/**
 * 认证业务结果码与登录审计结果映射
 *
 * @author Bunny
 */
@UtilityClass
public class AuthLoginAuditMapping {

	/**
	 * 业务异常对应的登录审计结果（凭证校验阶段）
	 */
	private static final Map<AuthResultCode, AuthLoginLogResult> CREDENTIAL_FAILURE_AUDIT = Map.of(
			AuthResultCode.AUTH_CODE_ERROR, AuthLoginLogResult.CAPTCHA_ERROR, AuthResultCode.USER_DISABLED,
			AuthLoginLogResult.ACCOUNT_DISABLED);

	/**
	 * 账号锁定类业务码对应的登录审计结果
	 */
	private static final Map<AuthResultCode, AuthLoginLogResult> LOCK_FAILURE_AUDIT = Map.of(AuthResultCode.USER_LOCKED,
			AuthLoginLogResult.ACCOUNT_LOCKED, AuthResultCode.USER_LOCKED_WITH_MINUTES,
			AuthLoginLogResult.ACCOUNT_LOCKED);

	/**
	 * 解析凭证校验阶段业务码对应的审计结果
	 * @param resultCode 业务结果码
	 * @return 审计结果，无映射时为空
	 */
	public static Optional<AuthLoginLogResult> credentialFailureAudit(AuthResultCode resultCode) {
		return Optional.ofNullable(CREDENTIAL_FAILURE_AUDIT.get(resultCode));
	}

	/**
	 * 解析账号锁定类业务码对应的审计结果
	 * @param resultCode 业务结果码
	 * @return 审计结果，无映射时为空
	 */
	public static Optional<AuthLoginLogResult> lockFailureAudit(AuthResultCode resultCode) {
		return Optional.ofNullable(LOCK_FAILURE_AUDIT.get(resultCode));
	}

}
