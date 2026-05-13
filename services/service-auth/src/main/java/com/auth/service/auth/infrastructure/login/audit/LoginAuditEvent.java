package com.auth.service.auth.infrastructure.login.audit;

import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;

/**
 * 登录与安全审计领域事件（进程内），载荷已包含请求快照。
 *
 * @param snapshot 请求上下文快照
 * @param eventType 事件类型
 * @param result 审计结果
 * @param userId 用户 ID，未识别可为 null
 * @param principal 用户名或邮箱等
 * @param failureReason 失败原因 messageKey 或说明；成功为 null
 * @param sessionId 会话标识（如 jti）
 * @author Bunny
 */
public record LoginAuditEvent(LoginAuditSnapshot snapshot, AuthLoginLogType eventType, AuthLoginLogResult result,
		Long userId, String principal, String failureReason, String sessionId) {
}
