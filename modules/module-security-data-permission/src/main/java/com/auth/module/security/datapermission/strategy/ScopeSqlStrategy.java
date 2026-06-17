package com.auth.module.security.datapermission.strategy;

import com.auth.module.security.contract.api.datascope.DataScopeStorageType;

import java.util.Set;

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
	Set<DataScopeStorageType> supportedTypes();

	/**
	 * 构建 SQL 条件
	 * @param context 上下文
	 * @return SQL 条件
	 */
	String buildCondition(ScopeSqlBuildContext context);

}
