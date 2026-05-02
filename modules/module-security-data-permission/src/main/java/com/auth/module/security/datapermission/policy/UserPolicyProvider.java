package com.auth.module.security.datapermission.policy;

import com.auth.module.security.contract.api.authorization.AuthProfile;

import java.util.List;

/**
 * 加载当前用户的数据策略
 *
 * <p>
 * TODO: 实现 Redis/会话 backed provider. MVP 默认提供者返回空列表
 * </p>
 *
 * @author Bunny
 */
public interface UserPolicyProvider {

	/**
	 * 加载策略
	 * @param profile 登录用户
	 * @return 策略列表
	 */
	List<DataPolicy> loadPolicies(AuthProfile profile);

}
