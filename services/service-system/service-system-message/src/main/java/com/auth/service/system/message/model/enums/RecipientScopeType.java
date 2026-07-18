package com.auth.service.system.message.model.enums;

import lombok.AllArgsConstructor;

/**
 * 站内信接收范围类型
 *
 * @author Bunny
 */
@AllArgsConstructor
public enum RecipientScopeType {

	/**
	 * 指定用户
	 */
	USER(DeliveryMode.PUSH),

	/**
	 * 指定岗位
	 */
	POST(DeliveryMode.PUSH),

	/**
	 * 指定部门
	 */
	DEPT(DeliveryMode.PUSH),

	/**
	 * 全员
	 */
	ALL(DeliveryMode.PULL);

	/**
	 * 该范围对应的投递模式
	 */
	private final DeliveryMode deliveryMode;

	/**
	 * 解析范围类型（大小写不敏感）
	 * @param raw 原始字符串
	 * @return 枚举；无法识别时返回 {@code null}
	 */
	public static RecipientScopeType from(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return RecipientScopeType.valueOf(raw.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	/**
	 * 该范围使用的投递模式
	 * @return 站内信投递模式
	 */
	public DeliveryMode deliveryMode() {
		return deliveryMode;
	}

}
