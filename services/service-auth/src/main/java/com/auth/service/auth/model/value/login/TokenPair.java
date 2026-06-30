package com.auth.service.auth.model.value.login;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 访问令牌与刷新令牌值对象
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class TokenPair {

	/**
	 * 访问令牌
	 */
	String accessToken;

	/**
	 * 刷新令牌
	 */
	String refreshToken;

	/**
	 * 访问令牌过期时间
	 */
	LocalDateTime accessExpiresAt;

}
