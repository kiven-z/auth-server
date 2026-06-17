package com.auth.service.auth.application.login.strategy;

import cn.hutool.crypto.digest.MD5;
import com.auth.service.auth.application.login.LoginAuditService;
import com.auth.service.auth.application.login.input.SmsCommand;
import com.auth.service.auth.application.port.authorization.AuthProfileQueryPort;
import com.auth.service.auth.application.port.login.CredentialPort;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.domain.authentication.model.enums.AuthLoginLogType;
import com.auth.service.auth.domain.authentication.model.enums.CredentialDimension;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 短信验证码登录策略。
 *
 * @author Bunny
 */
@Component
public class SmsLoginStrategy extends AbstractLoginStrategyTemplate<SmsCommand> {

	public SmsLoginStrategy(CredentialPort credentialGateway, AuthProfileQueryPort authProfileQueryPort,
			LoginAuditService loginAuditService) {
		super(credentialGateway, authProfileQueryPort, loginAuditService);
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_SMS;
	}

	@Override
	protected LoginAccount resolveAccount(SmsCommand command) {
		return credentialGateway.findForLogin(CredentialDimension.PHONE, command.getPhone())
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	@Override
	protected AuthLoginLogType doAuthenticate(SmsCommand command, LoginAccount account) {
		String phone = Objects.requireNonNull(command.getPhone());
		String codeDigest = MD5.create().digestHex16(Objects.requireNonNull(command.getCode()));
		String stored = credentialGateway.getStoredDigest(CredentialDimension.PHONE, phone);

		if (!Objects.equals(codeDigest, stored)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		credentialGateway.deleteCode(CredentialDimension.PHONE, phone);
		return AuthLoginLogType.LOGIN_SMS;
	}

	@Override
	public Class<SmsCommand> supports() {
		return SmsCommand.class;
	}

}
