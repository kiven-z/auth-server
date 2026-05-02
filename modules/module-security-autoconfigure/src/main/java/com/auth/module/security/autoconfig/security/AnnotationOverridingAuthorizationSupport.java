package com.auth.module.security.autoconfig.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * AnnotationOverridingAuthorizationManager 工具类
 *
 * @author Bunny
 */
@UtilityClass
public class AnnotationOverridingAuthorizationSupport {

	public static List<RequestMatcher> toPathMatchers(List<String> patterns) {
		if (patterns == null || patterns.isEmpty()) {
			return List.of();
		}
		List<RequestMatcher> out = new ArrayList<>();
		for (String p : patterns) {
			if (p == null || p.isBlank()) {
				continue;
			}
			out.add(PathPatternRequestMatcher.withDefaults().matcher(p.trim()));
		}
		return List.copyOf(out);
	}

	public static boolean matchesAny(List<RequestMatcher> matchers, HttpServletRequest request) {
		for (RequestMatcher matcher : matchers) {
			if (matcher.matches(request)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 与 {@code authenticated()} 语义对齐：匿名主体不算已认证
	 */
	public static boolean isNonAnonymousAuthenticated(Authentication authentication) {
		return authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken);
	}

}
