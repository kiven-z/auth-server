package com.auth.service.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth.service.auth.service.AuthMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auth.module.security.contract.redis.SecurityRedisKey.EMAIL_CODE;

/**
 * 认证消息服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AuthMessageServiceImpl implements AuthMessageService {

	private final RedisTemplate<String, Object> redisTemplate;

	public AuthMessageServiceImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendEmailCode(String email) {
		String redisKey = EMAIL_CODE.key(email);

		// TODO 后面接入邮件发送，并删除 log.debug
		// 存入Redis时需要使用MD5加密，取出时需要对比MD5值是否正确
		String code = RandomUtil.randomNumbers(6);
		log.debug("send email code to redis:{},code:{}", redisKey, code);
		redisTemplate.opsForValue().set(redisKey, MD5.create().digestHex16(code), EMAIL_CODE.getDefaultTtl());
	}

}
