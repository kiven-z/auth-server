package com.auth.service.auth.job;

import java.time.LocalDateTime;

import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.service.AuthorizationInvalidationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 授权失效幂等事件 SUCCESS 历史清理：删除已完成且超过保留期的记录
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AuthorizationInvalidationEventHistoryCleanupJob {

	private final AuthorizationInvalidationEventService authorizationInvalidationEventService;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * 定时清理已完成的过期幂等事件
	 */
	@Scheduled(fixedDelayString = "${auth.auth.invalidation.success-cleanup-interval-ms:86400000}")
	public void purgeCompletedHistory() {
		if (!properties.getSuccessCleanupEnabled()) {
			return;
		}

		LocalDateTime cutoff = LocalDateTime.now().minusDays(properties.getSuccessRetentionDays());
		int batchSize = properties.getSuccessCleanupBatchSize();
		int totalDeleted = purgeInBatches(cutoff, batchSize);
		if (totalDeleted > 0) {
			log.info("Purged completed invalidation events: count={}, cutoff={}", totalDeleted, cutoff);
		}
	}

	/**
	 * 分批删除，避免单次锁表过大
	 * @param cutoff processed_at 早于此时间的已完成行可被删除
	 * @param batchSize 每批上限
	 * @return 累计删除行数
	 */
	private int purgeInBatches(LocalDateTime cutoff, int batchSize) {
		int totalDeleted = 0;
		int deleted;
		do {
			deleted = authorizationInvalidationEventService.purgeCompletedBefore(cutoff, batchSize);
			totalDeleted += deleted;
		}
		while (deleted >= batchSize);
		return totalDeleted;
	}

}
