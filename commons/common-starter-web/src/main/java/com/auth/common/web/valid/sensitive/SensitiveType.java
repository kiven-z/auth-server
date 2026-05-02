package com.auth.common.web.valid.sensitive;

import lombok.Getter;

/**
 * 敏感数据类型枚举
 *
 * @author Bunny
 */

@Getter
public enum SensitiveType {

	/**
	 * 英文名
	 */
	ENGLISH_NAME,

	/**
	 * 中文名
	 */
	CHINESE_NAME,

	/**
	 * 身份证号
	 */
	ID_CARD,

	/**
	 * 手机号
	 */
	PHONE_NUMBER,

	/**
	 * 电子邮箱
	 */
	EMAIL,

	/**
	 * IP地址
	 */
	IP_ADDRESS,

	/**
	 * 银行卡号
	 */
	BANK_CARD,

	/**
	 * 自定义(需指定前后保留位数)
	 */
	CUSTOM

}
