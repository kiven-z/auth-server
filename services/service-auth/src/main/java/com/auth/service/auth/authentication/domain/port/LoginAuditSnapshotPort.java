package com.auth.service.auth.authentication.domain.port;

import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 从 HTTP 请求解析登录审计快照（IP/UA 等）。
 *
 * @author Bunny
 */
public interface LoginAuditSnapshotPort {

	/**
	 * 解析请求为审计快照；{@code request == null} 时返回空快照。
	 * @param request HTTP 请求，可为 null
	 * @return 不可变快照
	 */
	LoginAuditSnapshot build(HttpServletRequest request);

}
