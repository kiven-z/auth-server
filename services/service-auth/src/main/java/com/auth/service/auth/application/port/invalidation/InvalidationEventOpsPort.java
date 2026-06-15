package com.auth.service.auth.application.port.invalidation;

import java.time.LocalDateTime;

/**
 * 授权失效幂等事件运维 Port
 *
 * @author Bunny
 */
public interface InvalidationEventOpsPort {

	/**
	 * 按 eventId 查询事件投影
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
