package com.auth.module.security.autoconfigure.pipeline.authenticate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 将 AuthProfile 写入 SecurityContextHolder
 *
 * @author Bunny
 */
public final class AuthProfileSecurityContextPopulator {

	/**
	 * 构建授权权限
	 * @param profile 授权画像
	 * @return 授权权限
	 */
	private static List<GrantedAuthority> buildAuthorities(AuthProfile profile) {
		return Stream.concat(
				// 构建权限
				CollUtil.emptyIfNull(profile.getPermissions())
					.stream()
					.filter(StrUtil::isNotBlank)
					.map(StrUtil::trim)
					.map(SimpleGrantedAuthority::new),

				// 构建角色
				CollUtil.emptyIfNull(profile.getRoles())
					.stream()
					.filter(StrUtil::isNotBlank)
					.map(StrUtil::trim)
					.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
					.map(SimpleGrantedAuthority::new))
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
