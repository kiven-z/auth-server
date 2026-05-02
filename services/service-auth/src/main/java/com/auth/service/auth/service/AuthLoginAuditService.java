package com.auth.service.auth.service;

import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证事件审计写入（与 MyBatis-Plus {@code IService} 解耦，便于单元测试 Mock）。
 *
 * @author Bunny
 */
public interface AuthLoginAuditService {

	/**
	 * 记录一条认证相关审计（登录、登出、刷新令牌等）。
	 * @param request HTTP 请求，用于 IP/UA；可为 null，此时地域与设备字段不落库
	 * @param eventType 事件类型
	 * @param result 结果码（与 {@code log_login.login_result} 一致）
	 * @param userId 用户 ID，未识别可为 null
	 * @param principal 用户名或邮箱等冗余检索字段
	 * @param failureReason 失败原因（messageKey 或简短说明）；成功时可为 null
	 * @param sessionId 会话 ID（如 jti）；无可为空
	 */
	void recordLoginLog(HttpServletRequest request, AuthLoginLogType eventType, AuthLoginLogResult result, Long userId,
			String principal, String failureReason, String sessionId);

}
