package com.auth.service.system.authorization.job;

import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationOutboxProcessor;
import com.auth.service.system.authorization.outbox.AuthorizationInvalidationOutboxStatus;
import com.auth.service.system.authorization.outbox.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.outbox.model.SysAuthorizationInvalidationOutboxEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 授权失效 Outbox 补偿任务：扫描 PENDING / FAILED 并重试投递 auth。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class AuthorizationInvalidationOutboxJob {

	private static final List<String> CLAIMABLE_STATUSES = List.of(
			AuthorizationInvalidationOutboxStatus.PENDING.name(),
			AuthorizationInvalidationOutboxStatus.FAILED.name());

	private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

	private final AuthorizationInvalidationOutboxProcessor outboxProcessor;

	private final AuthorizationInvalidationProperties properties;

	@Value("${spring.application.name:service-system}")
	private String applicationName;

	private final String instanceSuffix = Long.toHexString(System.nanoTime());

	/**
	 * 定时扫描并补偿投递失败的 Outbox 记录。
	 */
	@Scheduled(fixedDelayString = "${auth.system.authorization-invalidation.poll-interval-ms:30000}")
	public void pollAndDispatch() {
		LocalDateTime now = LocalDateTime.now();
		List<SysAuthorizationInvalidationOutboxEntity> rows = outboxMapper.selectClaimable(CLAIMABLE_STATUSES, now,
				properties.getPollBatchSize());
		if (rows.isEmpty()) {
			return;
		}
		String lockedBy = applicationName + "-" + instanceSuffix;
		for (SysAuthorizationInvalidationOutboxEntity row : rows) {
			try {
				outboxProcessor.processClaimedRow(row, lockedBy);
			}
			catch (RuntimeException ex) {
				log.warn("Outbox compensation failed: outboxId={}, eventId={}, cause={}", row.getId(),
						row.getEventId(), ex.getMessage());
			}
		}
	}

}
