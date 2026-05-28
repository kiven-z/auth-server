package com.auth.module.security.contract.api.datascope;

import lombok.experimental.UtilityClass;

/**
 * 数据权限资源键（同一维度下区分不同业务表/查询）
 *
 * @author Bunny
 */
@UtilityClass
public class DataScopeResource {

	/**
	 * 默认资源键，与 {@code @DataScope(resourceCode)} 默认值一致
	 */
	public static final String DEFAULT = "default";

}
