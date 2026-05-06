package com.auth.module.security.datapermission.strategy;

import java.util.Set;

import com.auth.module.security.contract.api.granttable.ScopeGrantType;

/**
 * 数据范围 SQL 生成策略
 *
 * @author Bunny
 */
public interface ScopeSqlStrategy {

	/**
	 * 支持的范围类型
	 * @return 范围类型集合
	 */
	Set<ScopeGrantType> supportedTypes();

	/**
	 * 构建 SQL 条件
	 * @param context 上下文
	 * @return SQL 条件
	 */
	String buildCondition(ScopeSqlBuildContext context);

}
