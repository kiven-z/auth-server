package com.auth.service.auth.authorization.domain.invalidation.port;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.authorization.domain.invalidation.model.IdempotencyClaimStatus;

import java.util.Optional;

/**
 * 授权失效幂等端口（{@code eventId} 去重）。
 *
 * @author Bunny
 */
public interface InvalidationIdempotencyPort {

	/**
	 * 查询已处理事件的结果摘要（不含处理中占位行）。
	 * @param eventId 业务事件 ID
	 * @return 已存在时返回结果，否则为空
	 */
	Optional<AuthorizationInvalidateResponse> findProcessedOutcome(String eventId);

	/**
	 * 抢占 {@code eventId} 处理权（INSERT 占位行，依赖 {@code uk_event_id}）。
	 * @param eventId 业务事件 ID
	 * @param kind 变更维度
	 * @return 抢占结果
	 */
	IdempotencyClaimStatus tryClaim(String eventId, AuthorizationChangeKind kind);

	/**
	 * 将占位行更新为最终处理结果。
	 * @param eventId 业务事件 ID
	 * @param kind 变更维度
	 * @param response 执行结果
	 */
	void completeProcessedOutcome(String eventId, AuthorizationChangeKind kind,
			AuthorizationInvalidateResponse response);

	/**
	 * 处理失败时释放占位，便于 Outbox 重试。
	 * @param eventId 业务事件 ID
	 */
	void releaseClaim(String eventId);

}
