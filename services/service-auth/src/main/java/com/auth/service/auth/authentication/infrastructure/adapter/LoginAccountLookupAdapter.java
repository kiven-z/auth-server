package com.auth.service.auth.authentication.infrastructure.adapter;

import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.domain.port.LoginAccountLookupPort;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialField;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialQueries;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link LoginAccountLookupPort} 实现：委托 {@link UserMapper} 查询并映射为 {@link LoginAccount}。
 *
 * @author Bunny
 */
@Component
public class LoginAccountLookupAdapter implements LoginAccountLookupPort {

	private final UserMapper userMapper;

	public LoginAccountLookupAdapter(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	@Override
	public Optional<LoginAccount> findForLogin(CredentialDimension dimension, String value) {
		return Optional.ofNullable(UserCredentialQueries.forLogin(userMapper, toField(dimension), value))
			.map(LoginAccount::from);
	}

	@Override
	public Optional<LoginAccount> findActiveOnly(CredentialDimension dimension, String value) {
		return Optional.ofNullable(UserCredentialQueries.activeOnly(userMapper, toField(dimension), value))
			.map(LoginAccount::from);
	}

	private static UserCredentialField toField(CredentialDimension dimension) {
		return UserCredentialField.valueOf(dimension.name());
	}

}
