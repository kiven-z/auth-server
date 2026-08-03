package com.auth.service.system.authorization.dispatch.trigger;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.authorization.feign.AuthorizationInnerFeignClient;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 用户授权画像刷新触发器（admin 经 trigger 门面访问 auth 内部能力）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class UserProfileRefreshTrigger {

	private final AuthorizationInnerFeignClient authorizationInnerFeignClient;

	/**
	 * 批量刷新用户授权画像（去重、忽略空）
	 * @param userIds 用户主键
	 */
	public void refreshByUserIds(List<Long> userIds) {
		List<Long> ids = CollUtil.emptyIfNull(userIds).stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		Result<Void> result = authorizationInnerFeignClient.refreshProfiles(ids);
		if (!FeignUtil.isSuccess(result)) {
			log.warn("User profile refresh unavailable: size={}", ids.size());
			throw new SystemBusinessException(SystemCommonResultCode.SERVICE_UNAVAILABLE);
		}
	}

}
