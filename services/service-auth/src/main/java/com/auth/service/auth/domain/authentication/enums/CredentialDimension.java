package com.auth.service.auth.domain.authentication.enums;

/**
 * 登录凭证查询维度（与 {@code sys_user} 列语义一致）。
 *
 * @author Bunny
 */
public enum CredentialDimension {

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
