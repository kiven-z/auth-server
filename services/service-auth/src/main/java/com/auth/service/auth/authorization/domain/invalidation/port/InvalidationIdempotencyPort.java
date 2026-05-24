package com.auth.service.auth.authorization.domain.invalidation.port;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;

import java.util.Optional;

/**
 * 授权失效幂等端口（{@code eventId} 去重）。
 *
 * @author Bunny
 */
public interface InvalidationIdempotencyPort {

	/**
	 * 查询已处理事件的结果摘要。
	 * @param eventId 业务事件 ID
	 * @return 已存在时返回结果，否则为空
	 */
	Optional<AuthorizationInvalidateResponse> findProcessedOutcome(String eventId);

	/**
	 * 记录已成功处理的事件及结果摘要。
	 * @param eventId 业务事件 ID
	 * @param kind 变更维度
	 * @param response 执行结果
	 */
	void saveProcessedOutcome(String eventId, AuthorizationChangeKind kind, AuthorizationInvalidateResponse response);

}
