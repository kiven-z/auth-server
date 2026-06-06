package com.auth.service.system.authorization.ops.service;

import cn.hutool.core.util.BooleanUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationOutboxProcessor;
import com.auth.service.system.authorization.ops.mapper.AuthorizationInvalidationOutboxOpsMapper;
import com.auth.service.system.authorization.ops.model.AuthorizationInvalidationOutboxManualRetryOutcome;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationOutboxOpsQuery;
import com.auth.service.system.authorization.outbox.AuthorizationInvalidationOutboxStatus;
import com.auth.service.system.authorization.outbox.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.outbox.model.SysAuthorizationInvalidationOutboxEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.feign.AuthInnerFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static com.auth.service.system.authorization.ops.exception.AuthorizationInvalidationOpsResultCode.*;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 授权失效 Outbox 运维门面实现 FIXME 待修复里面方法归属
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationOutboxOpsServiceImpl implements AuthorizationInvalidationOutboxOpsService {

	private static final Set<String> RESET_REQUIRED_STATUSES = Set.of(AuthorizationInvalidationOutboxStatus.DEAD.name(),
			AuthorizationInvalidationOutboxStatus.FAILED.name(),
			AuthorizationInvalidationOutboxStatus.PROCESSING.name());

	private final AuthorizationInvalidationOutboxOpsMapper authorizationInvalidationOutboxOpsMapper;

	private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

	private final AuthorizationInvalidationOutboxProcessor outboxProcessor;

	private final AuthInnerFeignClient authInnerFeignClient;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPage<AuthorizationInvalidationOutboxPageRowPO> getPage(AuthorizationInvalidationOutboxOpsQuery query) {
		Page<AuthorizationInvalidationOutboxPageRowPO> pageParams = new Page<>(query.getPageIndex(),
				query.getPageSize());
		return authorizationInvalidationOutboxOpsMapper.selectListByPage(pageParams, query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxDetailRowPO getDetail(Long id) {
		return authorizationInvalidationOutboxOpsMapper.selectDetailById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxStatsPO getOutboxStats() {
		return authorizationInvalidationOutboxOpsMapper.selectOutboxStats();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public AuthorizationInvalidationOutboxManualRetryOutcome retryManual(Long outboxId, boolean force) {
		SysAuthorizationInvalidationOutboxEntity row = outboxMapper.selectById(outboxId);
		if (row == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		String previousStatus = row.getStatus();
		validateRetryable(row, force);

		boolean claimReleased = releaseEventClaimIfPresent(row.getEventId());
		if (RESET_REQUIRED_STATUSES.contains(previousStatus)) {
			resetForManualRetry(row);
		}

		boolean dispatched = outboxProcessor.processById(outboxId);
		SysAuthorizationInvalidationOutboxEntity latest = outboxMapper.selectById(outboxId);
		String currentStatus = latest != null ? latest.getStatus() : null;
		String lastError = latest != null ? latest.getLastError() : null;
		return new AuthorizationInvalidationOutboxManualRetryOutcome(outboxId, row.getEventId(), previousStatus,
				currentStatus, lastError, dispatched, claimReleased);
	}

	private void validateRetryable(SysAuthorizationInvalidationOutboxEntity row, boolean force) {
		String status = row.getStatus();
		if (AuthorizationInvalidationOutboxStatus.SUCCESS.name().equals(status)) {
			throw new SystemBusinessException(RETRY_NOT_ALLOWED, status);
		}
		if (!AuthorizationInvalidationOutboxStatus.PROCESSING.name().equals(status)) {
			return;
		}
		if (force) {
			return;
		}
		if (isProcessingUnlockAllowed(row.getLockedAt())) {
			return;
		}
		throw new SystemBusinessException(PROCESSING_LOCKED);
	}

	private boolean isProcessingUnlockAllowed(LocalDateTime lockedAt) {
		if (lockedAt == null) {
			return true;
		}
		long timeoutMinutes = properties.getManualProcessingUnlockTimeoutMinutes();
		return !lockedAt.plusMinutes(timeoutMinutes).isAfter(LocalDateTime.now());
	}

	private void resetForManualRetry(SysAuthorizationInvalidationOutboxEntity row) {
		int updated = outboxMapper.resetForManualRetry(row.getId(), row.getVersion());
		if (updated == 0) {
			throw new SystemBusinessException(RETRY_CONFLICT);
		}
	}

	private boolean releaseEventClaimIfPresent(String eventId) {
		Result<Boolean> result = authInnerFeignClient.releaseInvalidationEventClaim(eventId);
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Release invalidation event claim unavailable, eventId={}", eventId);
			return false;
		}
		return BooleanUtil.isTrue(result.getData());
	}

}
