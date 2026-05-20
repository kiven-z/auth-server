package com.auth.service.auth.authentication.infrastructure.persistence;

/**
 * 用户凭证查询维度（与 {@code sys_user} 列名一一对应，供动态 SQL 白名单使用）。
 *
 * @author Bunny
 */
public enum UserCredentialField {

	/**
	 * 用户名
	 */
	USERNAME,

	/**
	 * 邮箱
	 */
	EMAIL,

	/**
	 * 手机号
	 */
	PHONE

}
