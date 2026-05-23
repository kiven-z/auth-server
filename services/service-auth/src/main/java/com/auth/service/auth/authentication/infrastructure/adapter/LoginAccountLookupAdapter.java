package com.auth.service.auth.authentication.infrastructure.adapter;

import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialField;
import com.auth.service.auth.authentication.infrastructure.persistence.UserCredentialQueries;
import com.auth.service.auth.authentication.infrastructure.persistence.UserMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 委托 {@link UserMapper} 查询并映射为 {@link LoginAccount}。
 *
 * @author Bunny
 */
@Component
public class LoginAccountLookupAdapter {

	private final UserMapper userMapper;

	public LoginAccountLookupAdapter(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	/**
	 * 根据凭证维度查询登录账户
	 * @param dimension 凭证维度
	 * @param value 凭证值
	 * @return 登录账户
	 */
	public Optional<LoginAccount> findForLogin(CredentialDimension dimension, String value) {
		return Optional.ofNullable(UserCredentialQueries.forLogin(userMapper, toField(dimension), value))
			.map(LoginAccount::from);
	}

	/**
	 * 根据凭证维度查询当前有效的登录账户
	 * @param dimension 凭证维度
	 * @param value 凭证值
	 * @return 登录账户
	 */
	public Optional<LoginAccount> findActiveOnly(CredentialDimension dimension, String value) {
		return Optional.ofNullable(UserCredentialQueries.activeOnly(userMapper, toField(dimension), value))
			.map(LoginAccount::from);
	}

	/**
	 * 将凭证维度转换为用户凭证字段
	 * @param dimension 凭证维度
	 * @return 用户凭证字段
	 */
	private static UserCredentialField toField(CredentialDimension dimension) {
		return UserCredentialField.valueOf(dimension.name());
	}

}
