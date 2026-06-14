package com.auth.service.auth.infrastructure.audit;

import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.common.web.model.entity.UserAgent;
import com.auth.service.auth.application.login.port.LoginAuditSnapshotPort;
import com.auth.service.auth.domain.authentication.model.LoginAuditSnapshot;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 从当前 HTTP 请求构建 {@link LoginAuditSnapshot}，作为 {@link LoginAuditSnapshotPort} 的唯一实现。
 *
 * @author Bunny
 */
@Component
public class LoginAuditSnapshotFactory implements LoginAuditSnapshotPort {

	private final IpAddressService ipAddressService;

	public LoginAuditSnapshotFactory(IpAddressService ipAddressService) {
		this.ipAddressService = ipAddressService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
