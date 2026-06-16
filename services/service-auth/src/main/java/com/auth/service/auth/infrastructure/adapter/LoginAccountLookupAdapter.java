package com.auth.service.auth.infrastructure.adapter;

import com.auth.service.auth.domain.authentication.enums.CredentialDimension;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.infrastructure.persistence.UserMapper;
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
	 * 根据凭证维度查询登录账户（仅过滤已删除用户，禁用/锁定由策略层校验）
	 * @param dimension 凭证维度
	 * @param value 凭证值
	 * @return 登录账户
	 */
	public Optional<LoginAccount> findForLogin(CredentialDimension dimension, String value) {
		return Optional.ofNullable(userMapper.selectByCredential(dimension, value, false)).map(LoginAccount::from);
	}

	/**
	 * 根据凭证维度查询当前有效的登录账户（未删除且 status 正常）
	 * @param dimension 凭证维度
	 * @param value 凭证值
	 * @return 登录账户
	 */
	public Optional<LoginAccount> findActiveOnly(CredentialDimension dimension, String value) {
		return Optional.ofNullable(userMapper.selectByCredential(dimension, value, true)).map(LoginAccount::from);
	}

}
