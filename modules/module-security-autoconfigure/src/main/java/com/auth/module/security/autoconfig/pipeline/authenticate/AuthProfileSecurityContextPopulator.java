package com.auth.module.security.autoconfig.pipeline.authenticate;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 将 AuthProfile 写入 SecurityContextHolder
 *
 * @author Bunny
 */
public final class AuthProfileSecurityContextPopulator {

	/**
	 * 将列表转换为流，如果列表为空，则返回空流
	 * @param list 列表
	 * @param <T> 列表元素类型
	 * @return 流
	 */
	private static <T> Stream<T> nullSafeStream(List<T> list) {
		return list != null ? list.stream() : Stream.empty();
	}

	/**
	 * 构建授权权限
	 * @param profile 授权画像
	 * @return 授权权限
	 */
	@SuppressWarnings("java:S6204")
	private static List<GrantedAuthority> buildAuthorities(AuthProfile profile) {
		return Stream.concat(
				// 构建权限
				nullSafeStream(profile.getPermissions()).filter(CharSequenceUtil::isNotBlank)
					.map(p -> new SimpleGrantedAuthority(CharSequenceUtil.trim(p))),

				// 构建角色
				nullSafeStream(profile.getRoles()).filter(CharSequenceUtil::isNotBlank).map(r -> {
					String trim = CharSequenceUtil.trim(r);
					return trim.startsWith("ROLE_") ? trim : "ROLE_" + trim;
				}).map(SimpleGrantedAuthority::new))
			.collect(Collectors.toList());
	}

	/**
	 * 填充安全上下文
	 * @param profile 授权画像
	 */
	public void populate(AuthProfile profile) {
		List<GrantedAuthority> authorities = buildAuthorities(profile);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(profile, null,
				authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}
