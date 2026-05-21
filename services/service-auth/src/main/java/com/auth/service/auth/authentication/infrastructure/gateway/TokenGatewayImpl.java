package com.auth.service.auth.authentication.infrastructure.gateway;

import com.auth.common.jwt.model.JwtUserToken;
import com.auth.service.auth.authentication.application.gateway.TokenGateway;
import com.auth.service.auth.authentication.domain.model.TokenPair;
import com.auth.service.auth.authentication.infrastructure.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * {@link TokenGateway} 实现：委托 {@link TokenService} 签发与解析 JWT。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class TokenGatewayImpl implements TokenGateway {

	private final TokenService tokenService;

	@Override
	public TokenPair buildTokenPair(Long userId, String jti, Long permVersion) {
		return tokenService.buildTokenPair(userId, jti, permVersion);
	}

	@Override
	public Optional<JwtUserToken> parseAccessTokenSafe(String token) {
		return tokenService.parseAccessTokenSafe(token);
	}

	@Override
	public JwtUserToken parseRefreshToken(String token) {
		return tokenService.parseRefreshToken(token);
	}

}
