package com.auth.module.security.autoconfigure.autoconfigure.filter;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

import java.util.Set;

/**
 * 因为在此处使用了SpringSecurity，在每个服务引入时会在控制台输出密码，虽然不会影响程序的运行也不会有什么额外影响，只是看起来不输出
 * 同时为了防止在每个服务中挨个排除包，所以在此处使用了这个类，让其不会在控制台中输出密码
 *
 * @author Bunny
 */
public final class SecurityStarterAutoConfigurationImportFilter implements AutoConfigurationImportFilter {

	/**
	 * 项目走的是JWT / 令牌过滤器而非表单 + 内存用户也因此不需要这类配置
	 */
	private static final Set<String> SKIP = Set
		.of("org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration");

	@Override
	public boolean[] match(String[] classNames, AutoConfigurationMetadata metadata) {
		boolean[] matches = new boolean[classNames.length];
		for (int i = 0; i < classNames.length; i++) {
			matches[i] = classNames[i] == null || !SKIP.contains(classNames[i]);
		}
		return matches;
	}

}
