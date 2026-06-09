package com.auth.service.system.authorization.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.authorization.feign.AuthInnerFeignClient;
import com.auth.service.system.authorization.service.UserSessionRevocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 经 auth 内部接口撤销用户会话
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class UserSessionRevocationServiceImpl implements UserSessionRevocationService {

	private final AuthInnerFeignClient authInnerFeignClient;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void revokeAllSessions(List<Long> userIds) {
		if (CollUtil.isEmpty(userIds)) {
			return;
		}

		try {
			Result<Void> result = authInnerFeignClient.kickAllSessions(userIds);
			if (!FeignUtil.isSuccess(result)) {
				log.warn("Failed to revoke user sessions: userIds={}, message={}", userIds, result.getMessage());
			}
		}
		catch (RuntimeException ex) {
			log.warn("Failed to revoke user sessions: userIds={}, error={}", userIds, ex.getMessage());
		}
	}

}
