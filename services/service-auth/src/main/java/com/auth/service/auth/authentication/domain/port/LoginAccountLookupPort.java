package com.auth.service.auth.authentication.domain.port;

import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;

import java.util.Optional;

/**
 * 按凭证维度查询登录账户（基础设施通过 Mapper 实现）。
 *
 * @author Bunny
 */
public interface LoginAccountLookupPort {

	/**
	 * 登录场景：仅过滤已删除用户，禁用/锁定由策略层校验。
	 * @param dimension 凭证维度
	 * @param value 凭证值
	 * @return 登录账户，不存在时为空
	 */
	Optional<LoginAccount> findForLogin(CredentialDimension dimension, String value);

	/**
	 * 发码等场景：仅查询未删除且状态正常的用户。
	 * @param dimension 凭证维度
	 * @param value 凭证值
	 * @return 登录账户，不存在时为空
	 */
	Optional<LoginAccount> findActiveOnly(CredentialDimension dimension, String value);

}
