package com.auth.service.auth.authorization.domain.invalidation.model;

/**
 * {@code eventId} 幂等抢占结果。
 *
 * @author Bunny
 */
public enum IdempotencyClaimStatus {

	/**
	 * 本线程已抢占，可执行失效流水线。
	 */
	CLAIMED,

	/**
	 * 事件已处理完成，应直接返回已存结果。
	 */
	ALREADY_COMPLETED,

	/**
	 * 其它线程正在处理，调用方宜稍后重试（Outbox 补偿）。
	 */
	IN_PROGRESS

}
