package com.auth.service.system.authorization.feign.fallback;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.authorization.feign.SessionRevocationInnerFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 会话撤销内部 Feign 降级
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SessionRevocationInnerFeignClientFallback implements SessionRevocationInnerFeignClient {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<Void> kickAllSessions(List<Long> userIds) {
		log.error("SessionRevocationInnerFeignClientFallback: kickAllSessions failed, userIds={}", userIds);
		return Result.error("Session revocation service is unavailable");
	}

}
