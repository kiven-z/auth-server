package com.auth.module.security.core.token.provider;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import com.auth.common.jwt.api.JwtTokenProvider;
import com.auth.common.jwt.model.JwtUserToken;
import com.auth.module.security.contract.constants.SecurityTokenKind;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.model.SecurityTokenResult;
import io.jsonwebtoken.Claims;

import java.util.Date;

import static com.auth.module.security.contract.constants.SecurityExternalTokenConstants.TOKEN_TYPE;

/**
 * 抽象令牌提供者
 *
 * @author Bunny
 */
public abstract class AbstractSecurityTokenProvider implements SecurityTokenProvider {

	protected final JwtTokenProvider jwtTokenProvider;

	protected AbstractSecurityTokenProvider(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	/**
	 * 添加秒数
	 * @param base 基础时间
	 * @param seconds 秒数
	 * @return 时间
	 */
	protected Date plusSeconds(Date base, long seconds) {
		return new Date(base.getTime() + seconds * 1000L);
	}

	/**
	 * 验证令牌是否合法、是否过期、是否是对应的类型
	 * @param token 令牌
	 * @return 是否有效
	 */
	@Override
	public boolean verifyToken(String token) {
		if (CharSequenceUtil.isBlank(token)) {
			return false;
		}
		// 验证令牌是否合法
		boolean validated = jwtTokenProvider.validateToken(token);
		if (BooleanUtil.isFalse(validated)) {
			return false;
		}

		// 是否过期
		long remainingSeconds = jwtTokenProvider.getRemainingSeconds(token);
		if (remainingSeconds <= 0) {
			return false;
		}

		// 是否是对应的类型
		Claims claims = jwtTokenProvider.getClaims(token);
		Object object = claims.get(TOKEN_TYPE);
		if (object == null) {
			return false;
		}
		String value = object.toString();
		boolean valueIsNull = CharSequenceUtil.equalsIgnoreCase(value, "null");
		if (valueIsNull) {
			return false;
		}

		return CharSequenceUtil.equals(value, getTokenKind());
	}

	/**
	 * 获取令牌类型
	 * @return 令牌类型
	 */
	protected abstract String getTokenKind();

	/**
	 * 解析令牌
	 * @param token 令牌
	 * @return 解析结果
	 */
	@Override
	public SecurityTokenResult parseToken(String token) {
		// 验证令牌是否为空
		if (CharSequenceUtil.isBlank(token)) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_MISSING, "Token is missing.");
		}

		// 过期优先（便于调用方区分 expired vs invalid）
		long remainingSeconds = jwtTokenProvider.getRemainingSeconds(token);
		if (remainingSeconds <= 0) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_EXPIRED, "Token has expired.");
		}

		// 验证令牌是否有效
		boolean verified = verifyToken(token);
		if (BooleanUtil.isFalse(verified)) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_INVALID, "Token is invalid.");
		}

		// 解析令牌
		JwtUserToken jwtUserToken = jwtTokenProvider.parseToken(token);
		String tokenKind = getTokenKind();
		SecurityTokenKind kind = SecurityTokenKind.of(tokenKind);
		if (kind == null) {
			throw new SecurityTokenException(SecurityResultCodeEnum.TOKEN_KIND_MISMATCH,
					"Token kind is not supported: " + tokenKind);
		}
		SecurityTokenResult.SecurityTokenResultBuilder builder = SecurityTokenResult.builder()
			.kind(kind)
			.rawToken(token)
			.userToken(jwtUserToken);
		enrichTokenResult(builder, token, jwtUserToken, kind);
		return builder.build();
	}

	/**
	 * 子类在解析完成后补充 {@link SecurityTokenResult} 字段（例如外部 Access Token 的 {@code perm_version}）
	 * @param builder 构建器
	 * @param token 令牌
	 * @param jwtUserToken JWT用户令牌
	 * @param kind 令牌类型
	 */
	protected abstract void enrichTokenResult(SecurityTokenResult.SecurityTokenResultBuilder builder, String token,
			JwtUserToken jwtUserToken, SecurityTokenKind kind);

}
