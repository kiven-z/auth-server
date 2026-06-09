package com.auth.service.system.authorization.model.record;

/**
 * Outbox 人工重试执行摘要
 *
 * @param outboxId Outbox 主键
 * @param eventId 业务事件 ID
 * @param previousStatus 重试前状态
 * @param currentStatus 当前状态
 * @param lastError 最近失败原因
 * @param dispatched 是否已完成一次投递尝试
 * @param claimReleased 是否释放了 auth 侧 processing 占位
 * @author Bunny
 */
public record AuthorizationInvalidationOutboxManualRetryOutcome(Long outboxId, String eventId, String previousStatus,
		String currentStatus, String lastError, boolean dispatched, boolean claimReleased) {
}
