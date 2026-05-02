package com.auth.module.security.autoconfig.service;

import cn.hutool.core.util.BooleanUtil;
import com.auth.module.security.autoconfig.config.security.AuditPolicy;
import com.auth.module.security.autoconfig.config.security.SecurityConfigProperties;
import com.auth.module.security.autoconfig.security.SecurityRequirement;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import com.auth.module.security.core.matcher.PermissionMatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static com.auth.module.security.autoconfig.web.SecurityUserUtils.currentAuthProfile;
import static com.auth.module.security.contract.constants.PermissionConstant.isAdmin;
import static com.auth.module.security.contract.constants.PermissionConstant.isAdminPermission;

/**
 * Permission decision service.
 *
 * @author Bunny
 */
@Component("auth")
public class PermissionService {

	private final ApplicationEventPublisher publisher;

	private final SecurityConfigProperties securityConfigProperties;

	private final List<AuthorizationAuditHandler> auditHandlers;

	public PermissionService(ApplicationEventPublisher publisher, SecurityConfigProperties securityConfigProperties,
			List<AuthorizationAuditHandler> auditHandlers) {
		this.publisher = publisher;
		this.securityConfigProperties = securityConfigProperties;
		this.auditHandlers = auditHandlers;
	}

	/**
	 * 从当前请求获取 {@link SecurityRequirement}
	 * @return 安全要求枚举
	 */
	private static SecurityRequirement currentSecurityRequirement() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			return null;
		}
		HttpServletRequest request = attrs.getRequest();
		Object raw = request.getAttribute(SecurityRequirement.REQUEST_ATTRIBUTE);
		return raw instanceof SecurityRequirement req ? req : null;
	}

	/**
	 * 决定权限
	 * @param required 需要的权限
	 * @return 是否决定权限
	 */
	public boolean decide(String required) {
		AuthProfile profile = currentAuthProfile();
		if (profile == null || profile.getUserId() == null) {
			publishIfNeeded(SecurityEventType.DENIED, null, null, required, "UNAUTHENTICATED");
			return false;
		}

		Long userId = profile.getUserId();
		String username = profile.getUsername();
		List<String> roles = profile.getRoles();
		List<String> permissions = profile.getPermissions();

		// 是否是管理员Id、角色是否是管理员
		if (isAdmin(userId, roles)) {
			publishIfNeeded(SecurityEventType.GRANTED, userId, username, required,
					"BYPASS_SUPER_USER_ID_OR_BYPASS_ADMIN_ROLE");
			return true;
		}

		// 权限是否是管理员
		if (isAdminPermission(permissions)) {
			publishIfNeeded(SecurityEventType.GRANTED, userId, username, required, "BYPASS_GLOBAL_WILDCARD_PERMISSION");
			return true;
		}

		// 注解权限码是否匹配成功
		boolean isGranted = permissions.stream().anyMatch(granted -> PermissionMatcher.matches(granted, required));
		if (isGranted) {
			publishIfNeeded(SecurityEventType.GRANTED, userId, username, required, "MATCHED");
			return true;
		}

		publishIfNeeded(SecurityEventType.DENIED, userId, username, required, "ACCESS_DENIED");
		return false;
	}

	/**
	 * 按审计策略判断是否需要发布事件，然后 publish + 委托给 SPI 处理器
	 */
	private void publishIfNeeded(SecurityEventType type, Long userId, String username, String required, String reason) {
		if (BooleanUtil.isFalse(shouldAudit())) {
			return;
		}
		SecurityAuthorizationAuditPayloadEvent event = SecurityAuthorizationAuditPayloadEvent.builder()
			.eventType(type)
			.userId(userId)
			.username(username)
			.requiredAuthority(required)
			.decisionReason(reason)
			.exceptionMessage(null)
			.build();
		publisher.publishEvent(event);
		auditHandlers.forEach(h -> h.handle(event));
	}

	/**
	 * 根据 {@link AuditPolicy} 判断当前请求是否需要记录审计
	 */
	private boolean shouldAudit() {
		AuditPolicy policy = securityConfigProperties.getAuditPolicy();
		if (policy == null) {
			policy = AuditPolicy.PUBLIC_WITH_TOKEN_RECORD;
		}
		return switch (policy) {
			case NONE -> false;
			case ALL_RECORD -> true;
			case PUBLIC_NO_RECORD -> {
				SecurityRequirement requirement = currentSecurityRequirement();
				yield requirement != SecurityRequirement.PUBLIC;
			}
			// 能走到 decide() 一定有 token，因此等价于全部记录
			case PUBLIC_WITH_TOKEN_RECORD -> true;
		};
	}

}
