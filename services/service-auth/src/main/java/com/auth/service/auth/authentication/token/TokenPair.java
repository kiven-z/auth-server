package com.auth.service.auth.authentication.token;

import java.time.LocalDateTime;

/**
 * 访问令牌与刷新令牌值对象
 *
 * @param accessToken 访问令牌
 * @param refreshToken 刷新令牌
 * @param accessExpiresAt 访问令牌过期时间
 * @author Bunny
 */
public record TokenPair(String accessToken, String refreshToken, LocalDateTime accessExpiresAt) {
}
