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
	 * 所有者列用于 SELF 范围
	 */
	String userColumn() default "create_user";

	/**
	 * 组织列用于部门范围
	 */
	String deptColumn() default "dept_id";

	/**
	 * 目标表的表别名（可选）
	 */
	String alias() default "";

	/**
	 * 用于构建 SQL 条件的处理器
	 */
	Class<? extends DataScopeHandler> handler() default DefaultDataScopeHandler.class;

	/**
	 * 当为 true 时，处理器强制用户自定义策略（注解列被忽略）
	 */
	boolean mandatory() default false;

	/**
	 * 自定义策略匹配时的策略代码当 {@code mandatory=true} 时
	 */
	String policyCode() default "";

}
