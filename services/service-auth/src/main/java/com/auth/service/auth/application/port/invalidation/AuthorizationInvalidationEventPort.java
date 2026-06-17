package com.auth.service.auth.application.port.invalidation;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateResponse;
import com.auth.service.auth.domain.authorization.model.InvalidationIdempotencyGate;

import java.time.LocalDateTime;

/**
 * 授权失效幂等事件端口：幂等门禁与运维清理。
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationEventPort {

	/**
	 * 查询已处理结果或抢占 {@code eventId} 处理权（INSERT 占位行，依赖 {@code uk_event_id}）
	 * @param eventId 业务事件 ID
	 * @param kind 变更维度
	 * @return 幂等门禁结果
	 */
	InvalidationIdempotencyGate acquireGate(String eventId, AuthorizationChangeKind kind);

	/**
	 * 将占位行更新为最终处理结果
	 * @param eventId 业务事件 ID
	 * @param kind 变更维度
	 * @param response 执行结果
	 */
	void completeProcessedOutcome(String eventId, AuthorizationChangeKind kind,
			AuthorizationInvalidateResponse response);

	/**
	 * 处理失败时释放占位，便于 Outbox 重试
	 * @param eventId 业务事件 ID
	 * @return 是否实际释放
	 */
	boolean releaseClaim(String eventId);

	/**
	 * 按 eventId 查询事件受影响用户数
	 * @param eventId 业务事件 ID
	 * @return 受影响用户数，不存在时返回 null
	 */
	Integer findImpactedUserCountByEventId(String eventId);

	/**
	 * 批量删除超时的 processing 占位行
	 * @param cutoffTime 占位 update_time 早于此时间视为超时
	 * @param batchSize 本批删除上限
	 * @return 实际删除行数
	 */
	int deleteStaleProcessingClaims(LocalDateTime cutoffTime, int batchSize);

	/**
	 * 批量删除已完成的过期幂等事件
	 * @param cutoffTime processed_at 早于此时间的已完成行可被删除
	 * @param batchSize 本批删除上限
	 * @return 实际删除行数
	 */
	int deleteCompletedBefore(LocalDateTime cutoffTime, int batchSize);

}
