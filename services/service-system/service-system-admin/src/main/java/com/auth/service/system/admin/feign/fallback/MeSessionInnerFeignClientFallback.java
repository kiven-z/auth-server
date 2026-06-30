package com.auth.service.system.admin.feign.fallback;

import java.util.List;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.feign.MeSessionInnerFeignClient;
import com.auth.service.system.admin.feign.dto.UserSessionRemoteDTO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 个人中心会话内部 Feign 降级
 *
 * @author Bunny
 */
@Slf4j
@Component
public class MeSessionInnerFeignClientFallback implements MeSessionInnerFeignClient {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<List<UserSessionRemoteDTO>> listUserSessions(Long userId) {
		log.error("MeSessionInnerFeignClientFallback: listUserSessions failed, userId={}", userId);
		return Result.error("Session query service is unavailable");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<Void> kickUserSession(Long userId, String sessionId) {
		log.error("MeSessionInnerFeignClientFallback: kickUserSession failed, userId={}, sessionId={}", userId,
				sessionId);
		return Result.error("Session kick service is unavailable");
	}

}
