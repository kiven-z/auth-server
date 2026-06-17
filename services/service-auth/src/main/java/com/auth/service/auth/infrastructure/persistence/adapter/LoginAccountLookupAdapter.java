package com.auth.service.auth.infrastructure.persistence.adapter;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.domain.authentication.model.enums.CredentialDimension;
import com.auth.service.auth.infrastructure.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 委托 {@link UserMapper} 查询并映射为 {@link LoginAccount}。
 *
 * @author Bunny
 */
@Repository
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
		UserEntity userEntity = userMapper.selectByCredential(dimension, value, false);
		return Optional.ofNullable(userEntity).map(LoginAccount::from);
	}

	/**
	 * 根据凭证维度查询当前有效的登录账户（未删除且 status 正常）
	 * @param dimension 凭证维度
	 * @param value 凭证值
	 * @return 登录账户
	 */
	public Optional<LoginAccount> findActiveOnly(CredentialDimension dimension, String value) {
		UserEntity userEntity = userMapper.selectByCredential(dimension, value, true);
		return Optional.ofNullable(userEntity).map(LoginAccount::from);
	}

}
