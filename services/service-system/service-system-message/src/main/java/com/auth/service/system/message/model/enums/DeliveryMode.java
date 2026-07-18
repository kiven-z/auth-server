package com.auth.service.system.message.model.enums;

/**
 * 站内信投递模式
 *
 * @author Bunny
 */
public enum DeliveryMode {

	/**
	 * 读扩散：只写发送任务，用户状态懒落库
	 */
	PULL,

	/**
	 * 写扩散：展开接收人并预写收件箱
	 */
	PUSH;

	/**
	 * 解析投递模式（大小写不敏感）
	 * @param raw 原始字符串
	 * @return 枚举；无法识别时返回 {@code null}
	 */
	public static DeliveryMode from(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return DeliveryMode.valueOf(raw.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	/**
	 * 是否读扩散
	 * @return true=PULL
	 */
	public boolean isPull() {
		return this == PULL;
	}

}
