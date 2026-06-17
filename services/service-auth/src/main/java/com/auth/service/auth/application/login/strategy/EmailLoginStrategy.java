package com.auth.service.auth.application.login.strategy;

import cn.hutool.crypto.digest.MD5;
import com.auth.service.auth.application.login.LoginAuditService;
import com.auth.service.auth.application.login.input.EmailCommand;
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
 * 邮箱验证码登录策略。
 *
 * @author Bunny
 */
@Component
public class EmailLoginStrategy extends AbstractLoginStrategyTemplate<EmailCommand> {

	public EmailLoginStrategy(CredentialPort credentialGateway, AuthProfileQueryPort authProfileQueryPort,
			LoginAuditService loginAuditService) {
		super(credentialGateway, authProfileQueryPort, loginAuditService);
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	protected LoginAccount resolveAccount(EmailCommand command) {
		return credentialGateway.findForLogin(CredentialDimension.EMAIL, command.getEmail())
			.orElseThrow(() -> new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	@Override
	protected AuthLoginLogType doAuthenticate(EmailCommand command, LoginAccount account) {
		String email = Objects.requireNonNull(command.getEmail());
		String codeDigest = MD5.create().digestHex16(Objects.requireNonNull(command.getCode()));
		String stored = credentialGateway.getStoredDigest(CredentialDimension.EMAIL, email);

		if (!Objects.equals(codeDigest, stored)) {
			throw new AuthBusinessException(AuthResultCode.AUTH_CODE_ERROR);
		}

		credentialGateway.deleteCode(CredentialDimension.EMAIL, email);
		return AuthLoginLogType.LOGIN_EMAIL;
	}

	@Override
	public Class<EmailCommand> supports() {
		return EmailCommand.class;
	}

}
