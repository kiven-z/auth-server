package com.auth.service.auth.authentication.audit.event;

import com.auth.service.auth.support.model.enums.AuthLoginLogResult;
import com.auth.service.auth.support.model.enums.AuthLoginLogType;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 发布登录审计事件，由监听器统一落库
 *
 * @author Bunny
 */
public interface LoginAuditEventPublisher {

	/**
	 * 在当前线程解析请求快照并发布 {@link LoginAuditEvent}
	 * @param request HTTP 请求，可为 null
	 * @param eventType 事件类型
	 * @param result 审计结果
	 * @param userId 用户 ID
	 * @param principal 主体
	 * @param failureReason 失败原因
	 * @param sessionId 会话 ID
	 */
	void publish(HttpServletRequest request, AuthLoginLogType eventType, AuthLoginLogResult result, Long userId,
			String principal, String failureReason, String sessionId);

}
