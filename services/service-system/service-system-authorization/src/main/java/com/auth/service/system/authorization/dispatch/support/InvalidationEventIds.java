package com.auth.service.system.authorization.dispatch.support;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.experimental.UtilityClass;

/**
 * 授权失效生成格式 {kind}:{snowflakeId}}
 *
 * @author Bunny
 */
@UtilityClass
public class InvalidationEventIds {

	/**
	 * 生成幂等 eventId
	 * @param kind 变更维度
	 * @return eventId
	 */
	public static String next(AuthorizationChangeKind kind) {
		return kind.eventIdPrefix() + ":" + IdWorker.getId();
	}

}
