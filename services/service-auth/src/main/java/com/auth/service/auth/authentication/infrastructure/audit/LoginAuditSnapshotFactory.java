package com.auth.service.auth.authentication.infrastructure.audit;

import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.service.auth.authentication.domain.model.LoginAuditSnapshot;
import com.auth.common.web.model.entity.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 从当前 HTTP 请求构建 {@link LoginAuditSnapshot}
 *
 * @author Bunny
 */
@Component
public class LoginAuditSnapshotFactory {

	private final IpAddressService ipAddressService;

	public LoginAuditSnapshotFactory(IpAddressService ipAddressService) {
		this.ipAddressService = ipAddressService;
	}

	/**
	 * 解析请求为审计快照；{@code request == null} 时返回 {@link LoginAuditSnapshot#empty()}
	 * @param request HTTP 请求，可为 null
	 * @return 不可变快照
	 */
	public LoginAuditSnapshot build(HttpServletRequest request) {
		if (request == null) {
			return LoginAuditSnapshot.empty();
		}
		IpInfo ipInfo = ipAddressService.resolveIpInfo(request);
		UserAgent userAgent = UserAgent.getUserAgent(request);
		return new LoginAuditSnapshot(ipInfo.getIpAddr(), ipInfo.getIpRegion(), request.getHeader("User-Agent"),
				userAgent.getDeviceType(), userAgent.getOs(), userAgent.getBrowser());
	}

}
