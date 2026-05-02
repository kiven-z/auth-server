package com.auth.module.security.datapermission.policy;

import com.auth.module.security.contract.api.authorization.AuthProfile;

/**
 * 一个处理器，可以将 {@link DataPolicy} 转换为 SQL 条件
 *
 * <p>
 * TODO: 返回参数化条件而不是原始字符串
 * </p>
 *
 * @author Bunny
 */
public interface PolicyDataScopeHandler {

	/**
	 * 是否支持
	 * @param policy 策略
	 * @return 是否支持
	 */
	boolean supports(DataPolicy policy);

	/**
	 * 构建条件
	 * @param profile 登录用户
	 * @param policy 策略
	 * @return 条件
	 */
	String buildCondition(AuthProfile profile, DataPolicy policy);

}
