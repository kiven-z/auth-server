package com.auth.service.auth.authentication.infrastructure.gateway;

import com.auth.service.auth.authentication.application.gateway.CredentialGateway;
import com.auth.service.auth.authentication.domain.enums.CredentialDimension;
import com.auth.service.auth.authentication.domain.model.LoginAccount;
import com.auth.service.auth.authentication.infrastructure.adapter.LoginAccountLookupAdapter;
import com.auth.service.auth.authentication.infrastructure.adapter.RedisLoginVerificationCodeAdapter;
import com.auth.service.auth.authentication.infrastructure.token.LoginFailureRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * {@link CredentialGateway} 实现：委托账户查询、验证码 Redis 与登录失败限流。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class CredentialGatewayImpl implements CredentialGateway {

	private final LoginAccountLookupAdapter loginAccountLookupAdapter;

	private final RedisLoginVerificationCodeAdapter redisLoginVerificationCodeAdapter;

	private final LoginFailureRateLimiter loginFailureRateLimiter;

	@Override
	public Optional<LoginAccount> findForLogin(CredentialDimension dimension, String value) {
		return loginAccountLookupAdapter.findForLogin(dimension, value);
	}

	@Override
	public Optional<LoginAccount> findActiveOnly(CredentialDimension dimension, String value) {
		return loginAccountLookupAdapter.findActiveOnly(dimension, value);
	}

	@Override
	public String getStoredDigest(CredentialDimension dimension, String target) {
		return redisLoginVerificationCodeAdapter.getStoredDigest(dimension, target);
	}

	@Override
	public void deleteCode(CredentialDimension dimension, String target) {
		redisLoginVerificationCodeAdapter.deleteCode(dimension, target);
	}

	@Override
	public void storeDigest(CredentialDimension dimension, String target, String digestHex16) {
		redisLoginVerificationCodeAdapter.storeDigest(dimension, target, digestHex16);
	}

	@Override
	public void assertNotLocked(Long userId) {
		loginFailureRateLimiter.assertNotLocked(userId);
	}

	@Override
	public int recordFailure(Long userId) {
		return loginFailureRateLimiter.recordFailure(userId);
	}

	@Override
	public void recordSuccess(Long userId) {
		loginFailureRateLimiter.recordSuccess(userId);
	}

}
