package com.auth.service.auth.authentication.application.login.audit;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.auth.authentication.application.port.AuthProfileCachePort;
import com.auth.service.auth.authorization.domain.model.UserAccountSnapshot;
import com.auth.service.auth.authorization.domain.port.UserReadPort;
import org.springframework.stereotype.Component;

/**
 * 登录审计主体规范化：有 {@code userId} 时落库系统登录名，无 {@code userId} 时保留登录尝试主体。
 *
 * @author Bunny
 */
@Component
public class LoginAuditPrincipalNormalizer {

	/**
	 * 占位符，写入前需替换为真实登录名。
	 */
	private static final String LEGACY_PLACEHOLDER = "/";

	private final AuthProfileCachePort authProfileCachePort;

	private final UserReadPort userReadPort;

	public LoginAuditPrincipalNormalizer(AuthProfileCachePort authProfileCachePort, UserReadPort userReadPort) {
		this.authProfileCachePort = authProfileCachePort;
		this.userReadPort = userReadPort;
	}

	private static String blankToNull(String value) {
		return CharSequenceUtil.isBlank(value) ? null : value.trim();
	}

	/**
	 * 规范化审计主体。
	 * @param userId 用户 ID；为 null 时返回尝试主体（手机号/邮箱/输入用户名等）
	 * @param incomingPrincipal 调用方传入的主体，可为 null
	 * @return 规范化后的主体；无法解析且 userId 非空时可能为 null
	 */
	public String normalize(Long userId, String incomingPrincipal) {
		if (userId == null) {
			return blankToNull(incomingPrincipal);
		}

		String trimmed = blankToNull(incomingPrincipal);
		boolean isUsableAccountPrincipal = CharSequenceUtil.isNotBlank(trimmed) && !LEGACY_PLACEHOLDER.equals(trimmed);
		if (isUsableAccountPrincipal) {
			return trimmed;
		}

		return authProfileCachePort.loadCachedProfile(userId)
			.map(AuthProfile::getUsername)
			.filter(CharSequenceUtil::isNotBlank)
			.or(() -> userReadPort.findById(userId).map(UserAccountSnapshot::username))
			.orElse(null);
	}

}
