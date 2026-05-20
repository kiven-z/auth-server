package com.auth.service.auth.authentication.domain.port;

import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录审计落库端口：由基础设施实现，供事件监听器等调用。
 *
 * @author Bunny
 */
public interface LoginAuditRecorder {

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

	/**
	 * 使用已解析的请求快照记录审计（供事件监听器等路径调用）。
	 * @param snapshot 请求快照，不可为 null；无请求信息时使用 {@link LoginAuditSnapshot#empty()}
	 * @param eventType 事件类型
	 * @param result 结果码
	 * @param userId 用户 ID
	 * @param principal 主体
	 * @param failureReason 失败原因
	 * @param sessionId 会话 ID
	 */
	void recordLoginLog(LoginAuditSnapshot snapshot, AuthLoginLogType eventType, AuthLoginLogResult result, Long userId,
			String principal, String failureReason, String sessionId);

}
