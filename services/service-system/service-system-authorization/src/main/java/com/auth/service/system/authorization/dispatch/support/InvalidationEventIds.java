package com.auth.service.system.authorization.dispatch.support;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.experimental.UtilityClass;

/**
 * 授权失效生成格式 {kind}:{snowflakeId}}
 *
 * @author Bunny
 */
@UtilityClass
public class InvalidationEventIds {

	/**
	 * 部门维度短前缀
	 */
	public static final String KIND_DEPT = "dept";

	/**
	 * 角色维度短前缀
	 */
	public static final String KIND_ROLE = "role";

	/**
	 * 权限维度短前缀
	 */
	public static final String KIND_PERM = "perm";

	/**
	 * 岗位维度短前缀
	 */
	public static final String KIND_POST = "post";

	/**
	 * grant_table 维度短前缀
	 */
	public static final String KIND_GRANT = "grant";

	/**
	 * 生成幂等 eventId
	 * @param kind 变更维度短前缀
	 * @return eventId
	 */
	public static String next(String kind) {
		return kind + ":" + IdWorker.getId();
	}

}
