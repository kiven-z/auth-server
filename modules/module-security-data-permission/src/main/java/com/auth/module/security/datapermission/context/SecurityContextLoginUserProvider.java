package com.auth.module.security.datapermission.context;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import lombok.extern.slf4j.Slf4j;

/**
 * 反射式提供者，当 Spring SecurityContext 存在时，从其中读取 {@code AuthProfile}
 *
 * @author Bunny
 */
@Slf4j
public class SecurityContextLoginUserProvider implements LoginUserProvider {

	/**
	 * 获取当前登录用户
	 * @return 当前登录用户
	 */
	@Override
	public AuthProfile currentUser() {
		try {
			// 获取 SecurityContextHolder 类
			Class<?> holder = Class.forName("org.springframework.security.core.context.SecurityContextHolder");

			// 获取 SecurityContext
			Object context = holder.getMethod("getContext").invoke(null);
			if (context == null) {
				return null;
			}

			// 获取 Authentication
			Object auth = context.getClass().getMethod("getAuthentication").invoke(context);
			if (auth == null) {
				return null;
			}

			// 获取 Principal
			Object principal = auth.getClass().getMethod("getPrincipal").invoke(auth);
			if (principal instanceof AuthProfile profile) {
				return profile;
			}

			// 返回 null
			return null;
		}
		catch (Exception ex) {
			log.error("获取当前登录用户失败", ex);
			return null;
		}
	}

}
