package com.auth.common.data.model;

import lombok.Getter;

/**
 * 审计元数据字段枚举 用于标识实体的审计字段（创建人、创建时间等）
 *
 * @author Bunny
 */
@Getter
public enum DatabaseAuditMetaFieldEnum {

	/**
	 * 创建用户
	 */
	CREATE_USER("createUser"),

	/**
	 * 修改用户
	 */
	UPDATE_USER("updateUser"),

	/**
	 * 创建时间
	 */
	CREATE_TIME("createTime"),

	/**
	 * 修改时间
	 */
	UPDATE_TIME("updateTime");

	private final String columnName;

	DatabaseAuditMetaFieldEnum(String columnName) {
		this.columnName = columnName;
	}

}
