package com.auth.service.system.authorization.job;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.mapper.SysAuthorizationInvalidationOutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 授权失效 Outbox SUCCESS 历史清理：删除已完成且超过保留期的投递记录
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AuthorizationInvalidationOutboxHistoryCleanupJob {

	private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * 定时清理 SUCCESS 且已过保留期的 Outbox 记录
	 */
	@Scheduled(fixedDelayString = "${auth.system.authorization-invalidation.success-cleanup-interval-ms:86400000}")
	public void purgeSuccessHistory() {
		if (!BooleanUtil.isTrue(properties.getSuccessCleanupEnabled())) {
			return;
		}

		LocalDateTime cutoff = LocalDateTime.now().minusDays(properties.getSuccessRetentionDays());
		int batchSize = properties.getSuccessCleanupBatchSize();
		int totalDeleted = purgeInBatches(cutoff, batchSize);
		if (totalDeleted > 0) {
			log.info("Purged SUCCESS invalidation outbox rows: count={}, cutoff={}", totalDeleted, cutoff);
		}
	}

	/**
	 * 分批删除，避免单次锁表过大
	 * @param cutoff processed_at 早于此时间可被删除
	 * @param batchSize 每批上限
	 * @return 累计删除行数
	 */
	private int purgeInBatches(LocalDateTime cutoff, int batchSize) {
		int totalDeleted = 0;
		while (true) {
			int deleted = outboxMapper.deleteSuccessBefore(cutoff, batchSize);
			totalDeleted += deleted;
			if (deleted < batchSize) {
				break;
			}
		}
		return totalDeleted;
	}

}
