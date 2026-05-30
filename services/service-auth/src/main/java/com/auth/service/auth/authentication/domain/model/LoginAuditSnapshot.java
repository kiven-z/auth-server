package com.auth.service.auth.authentication.domain.model;

/**
 * 登录审计请求侧快照：在发布线程从 HTTP 请求解析后固化，供事件监听器持久化。
 *
 * @param loginIp 客户端 IP
 * @param loginRegion 地域描述
 * @param userAgent 原始 User-Agent 头
 * @param deviceType 设备类型
 * @param osType 操作系统
 * @param browserType 浏览器
 * @author Bunny
 */
public record LoginAuditSnapshot(String loginIp, String loginRegion, String userAgent, String deviceType, String osType,
		String browserType) {

	/**
	 * 无 HTTP 请求时的空快照（字段均为 null）。
	 * @return 空快照
	 */
	public static LoginAuditSnapshot empty() {
		return new LoginAuditSnapshot(null, null, null, null, null, null);
	}

}
