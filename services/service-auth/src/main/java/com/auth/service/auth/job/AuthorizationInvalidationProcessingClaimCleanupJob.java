package com.auth.service.auth.job;

import java.time.LocalDateTime;

import com.auth.service.auth.config.AuthorizationInvalidationProperties;
import com.auth.service.auth.service.AuthorizationInvalidationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 授权失效幂等事件 processing 占位超时清理：删除孤儿占位行，允许 Outbox 重新投递
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AuthorizationInvalidationProcessingClaimCleanupJob {

	private final AuthorizationInvalidationEventService authorizationInvalidationEventService;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * 定时扫描并释放超时的 processing 占位
	 */
	@Scheduled(fixedDelayString = "${auth.auth.invalidation.processing-claim-cleanup-interval-ms:300000}")
	public void cleanupStaleProcessingClaims() {
		if (!properties.getProcessingClaimCleanupEnabled()) {
			return;
		}

		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(properties.getProcessingClaimTimeoutMinutes());
		int batchSize = properties.getProcessingClaimCleanupBatchSize();
		int deleted = authorizationInvalidationEventService.cleanupStaleProcessingClaims(cutoff, batchSize);
		if (deleted > 0) {
			log.warn("Released stale invalidation processing claims: count={}, cutoff={}", deleted, cutoff);
		}
	}

}
