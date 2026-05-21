package com.auth.service.auth.authentication.domain.port;

import com.auth.service.auth.authentication.domain.enums.AuthLoginLogResult;
import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;

/**
 * 登录审计落库端口：由基础设施异步实现；调用方须在请求线程传入已解析的 {@link LoginAuditSnapshot}。
 *
 * @author Bunny
 */
public interface LoginAuditRecorder {

	/**
	 * 记录一条认证相关审计（登录、登出、刷新令牌等）；实现类通常以 {@code @Async} 落库。
	 * @param snapshot 请求快照，不可为 null；无请求信息时使用 {@link LoginAuditSnapshot#empty()}
	 * @param eventType 事件类型
	 * @param result 结果码（与 {@code log_login.login_result} 一致）
	 * @param userId 用户 ID，未识别可为 null
	 * @param principal 用户名或邮箱等冗余检索字段
	 * @param failureReason 失败原因（messageKey 或简短说明）；成功时可为 null
	 * @param sessionId 会话 ID（如 jti）；无可为空
	 */
	void recordLoginLog(LoginAuditSnapshot snapshot, AuthLoginLogType eventType, AuthLoginLogResult result, Long userId,
			String principal, String failureReason, String sessionId);

}
