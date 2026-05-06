package com.auth.module.security.datapermission.annotation;

import com.auth.module.security.datapermission.handler.DataScopeHandler;
import com.auth.module.security.datapermission.handler.DefaultDataScopeHandler;

import java.lang.annotation.*;

/**
 * 申明一个 mapper 方法需要数据范围过滤
 *
 * @author Bunny
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

	/**
	 * 用户所有者列，用于 SELF 范围过滤，默认 create_user
	 */
	String userColumn() default "create_user";

	/**
	 * 维度列名，例如 dept_id、post_id
	 */
	String dimensionColumn() default "dept_id";

	/**
	 * 表别名
	 */
	String alias() default "d";

	/**
	 * SQL 处理器
	 */
	Class<? extends DataScopeHandler> handler() default DefaultDataScopeHandler.class;

	/**
	 * 要匹配的数据资源键
	 */
	String resourceCode() default "default";

	/**
	 * 数据维度类型，如 DEPT、POST
	 */
	String dimension() default "DEPT";

	/**
	 * 是否忽略管理员
	 */
	boolean ignoreSuperAdmin() default false;

}
