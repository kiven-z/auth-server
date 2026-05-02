package com.auth.module.security.datapermission.policy;

import com.auth.module.security.contract.api.authorization.AuthProfile;

import java.util.List;

/**
 * 默认策略提供者：没有自定义策略
 *
 * @author Bunny
 */
public class DefaultUserPolicyProvider implements UserPolicyProvider {

	/**
	 * 加载策略
	 * @param profile 登录用户
	 * @return 策略列表
	 */
	@Override
	public List<DataPolicy> loadPolicies(AuthProfile profile) {
		return List.of();
	}

}
