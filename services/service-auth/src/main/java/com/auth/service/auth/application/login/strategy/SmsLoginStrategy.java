package com.auth.service.auth.application.login.strategy;

import cn.hutool.crypto.digest.MD5;
import com.auth.service.auth.application.login.audit.LoginAuditService;
import com.auth.service.auth.application.login.input.credential.SmsLogin;
import com.auth.service.auth.application.port.AuthProfileQueryPort;
import com.auth.service.auth.application.port.CredentialGateway;
import com.auth.service.auth.domain.authentication.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.enums.CredentialDimension;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 短信验证码登录策略。
 *
 * @author Bunny
 */
@Component
public class SmsLoginStrategy extends AbstractLoginStrategyTemplate<SmsLogin> {

	public SmsLoginStrategy(CredentialGateway credentialGateway, AuthProfileQueryPort authProfileQueryPort,
			LoginAuditService loginAuditService) {
		super(credentialGateway, authProfileQueryPort, loginAuditService);
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_SMS;
	}

	@Override
	protected LoginAccount resolveAccount(SmsLogin credential) {
		return credentialGateway.findForLogin(CredentialDimension.PHONE, credential.getPhone())
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	@Override
	protected AuthLoginLogType doAuthenticate(SmsLogin credential, LoginAccount account) {
		String phone = Objects.requireNonNull(credential.getPhone());
		String codeDigest = MD5.create().digestHex16(Objects.requireNonNull(credential.getCode()));
		String stored = credentialGateway.getStoredDigest(CredentialDimension.PHONE, phone);

		if (!Objects.equals(codeDigest, stored)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		credentialGateway.deleteCode(CredentialDimension.PHONE, phone);
		return AuthLoginLogType.LOGIN_SMS;
	}

	@Override
	public Class<SmsLogin> supports() {
		return SmsLogin.class;
	}

}
