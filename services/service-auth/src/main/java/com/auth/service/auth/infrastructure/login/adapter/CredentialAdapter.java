package com.auth.service.auth.infrastructure.login.adapter;

import com.auth.service.auth.application.port.login.CredentialPort;
import com.auth.service.auth.domain.authentication.model.LoginAccount;
import com.auth.service.auth.domain.authentication.model.enums.CredentialDimension;
import com.auth.service.auth.infrastructure.persistence.adapter.LoginAccountLookupAdapter;
import com.auth.service.auth.infrastructure.redis.adapter.RedisLoginVerificationCodeAdapter;
import com.auth.service.auth.infrastructure.token.LoginFailureRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * {@link CredentialPort} 适配器：委托账户查询、验证码 Redis 与登录失败限流。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Repository
public class CredentialAdapter implements CredentialPort {

	private final LoginAccountLookupAdapter loginAccountLookupAdapter;

	private final RedisLoginVerificationCodeAdapter redisLoginVerificationCodeAdapter;

	private final LoginFailureRateLimiter loginFailureRateLimiter;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<LoginAccount> findForLogin(CredentialDimension dimension, String value) {
		return loginAccountLookupAdapter.findForLogin(dimension, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<LoginAccount> findActiveOnly(CredentialDimension dimension, String value) {
		return loginAccountLookupAdapter.findActiveOnly(dimension, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStoredDigest(CredentialDimension dimension, String target) {
		return redisLoginVerificationCodeAdapter.getStoredDigest(dimension, target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteCode(CredentialDimension dimension, String target) {
		redisLoginVerificationCodeAdapter.deleteCode(dimension, target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeDigest(CredentialDimension dimension, String target, String digestHex16) {
		redisLoginVerificationCodeAdapter.storeDigest(dimension, target, digestHex16);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void assertNotLocked(Long userId) {
		loginFailureRateLimiter.assertNotLocked(userId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int recordFailure(Long userId) {
		return loginFailureRateLimiter.recordFailure(userId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordSuccess(Long userId) {
		loginFailureRateLimiter.recordSuccess(userId);
	}

}
