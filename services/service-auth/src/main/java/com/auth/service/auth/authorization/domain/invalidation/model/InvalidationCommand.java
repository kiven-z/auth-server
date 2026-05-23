package com.auth.service.auth.authorization.domain.invalidation.model;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * 授权失效命令（应用层入参）：携带类型化业务键与幂等事件 ID。
 *
 * @param target 失效目标
 * @param eventId 业务事件 ID，供后续幂等去重（当前阶段仅校验非空）
 * @author Bunny
 */
public record InvalidationCommand(ImpactTarget target, String eventId) {

	/**
	 * 紧凑构造：校验必填字段。
	 * @param target 失效目标
	 * @param eventId 事件 ID
	 */
	public InvalidationCommand {
		if (target == null) {
			throw new IllegalArgumentException("target must not be null");
		}
		if (CharSequenceUtil.isBlank(eventId)) {
			throw new IllegalArgumentException("eventId must not be blank");
		}
	}

}
