package com.auth.module.security.autoconfig.security;

import com.auth.module.security.autoconfig.config.security.SecurityConfigProperties;
import com.auth.module.security.autoconfig.pipeline.filter.TokenAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.auth.module.security.autoconfig.security.AnnotationOverridingAuthorizationSupport.*;

/**
 * 在 HTTP 授权阶段优先尊重 {@link SecurityRequirement}（由 {@link TokenAuthenticationFilter} 写入
 * {@link SecurityRequirement#REQUEST_ATTRIBUTE}），再回落到 {@code permitPaths} 路径规则
 *
 * @author Bunny
 * @see TokenAuthenticationFilter
 */
public class AnnotationOverridingAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

	private final SecurityConfigProperties securityProperties;

	private final Object matcherCacheLock = new Object();

	@Nullable
	private List<String> permitPathsIdentity;

	private List<RequestMatcher> permitMatchersCache = List.of();

	public AnnotationOverridingAuthorizationManager(SecurityConfigProperties securityProperties) {
		this.securityProperties = Objects.requireNonNull(securityProperties, "securityProperties");
	}

	private List<RequestMatcher> currentPermitMatchers() {
		List<String> paths = this.securityProperties.getPermitPaths();
		if (paths == null) {
			paths = List.of();
		}
		synchronized (this.matcherCacheLock) {
			if (paths != this.permitPathsIdentity) {
				this.permitPathsIdentity = paths;
				this.permitMatchersCache = toPathMatchers(paths);
			}
			return this.permitMatchersCache;
		}
	}

	@Override
	@Nullable
	public AuthorizationResult authorize(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
		HttpServletRequest request = context.getRequest();
		Object raw = request.getAttribute(SecurityRequirement.REQUEST_ATTRIBUTE);
		if (raw instanceof SecurityRequirement requirement) {
			if (requirement == SecurityRequirement.PUBLIC) {
				return new AuthorizationDecision(true);
			}
			if (requirement == SecurityRequirement.AUTHENTICATED) {
				return new AuthorizationDecision(isNonAnonymousAuthenticated(authentication.get()));
			}
			// FALLBACK_TO_PATH：继续走路径规则
		}

		List<RequestMatcher> permitMatchers = currentPermitMatchers();
		if (matchesAny(permitMatchers, request)) {
			return new AuthorizationDecision(true);
		}
		// 与历史 anyRequest().authenticated() 一致：默认需认证
		return new AuthorizationDecision(isNonAnonymousAuthenticated(authentication.get()));
	}

	/**
	 * 委托 {@link #authorize(Supplier, RequestAuthorizationContext)}；保留以满足当前
	 * {@link AuthorizationManager} 对抽象 {@code check} 的编译要求 NOSONAR S1133 — Spring
	 * Security 6.5 仍声明抽象 check，需保留桥接
	 * @deprecated 请改用 {@link #authorize(Supplier, RequestAuthorizationContext)}
	 */
	@Override
	@Deprecated
	public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
		AuthorizationResult r = authorize(authentication, context);
		if (r instanceof AuthorizationDecision ad) {
			return ad;
		}
		return new AuthorizationDecision(r != null && r.isGranted());
	}

}
