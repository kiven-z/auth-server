package com.auth.module.security.datapermission.handler;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.datapermission.annotation.DataScope;

/**
 * 默认处理器用于部门/自身过滤
 *
 * @author Bunny
 */
public class DefaultDataScopeHandler implements DataScopeHandler {

	/**
	 * 构建当前用户的 SQL 条件（仅 USER/ROLE/DEPT 三类主体的范围合并后作用于组织列）
	 */
	@Override
	public String buildCondition(AuthProfile profile, DataScope dataScope) {

		return null;
	}

}
