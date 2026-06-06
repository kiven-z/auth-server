package com.auth.service.system.admin.service.impl;

import static com.auth.service.system.admin.exception.SystemAdminResultCode.AUTH_INVALIDATION_OUTBOX_PROCESSING_LOCKED;
import static com.auth.service.system.admin.exception.SystemAdminResultCode.AUTH_INVALIDATION_OUTBOX_RETRY_CONFLICT;
import static com.auth.service.system.admin.exception.SystemAdminResultCode.AUTH_INVALIDATION_OUTBOX_RETRY_NOT_ALLOWED;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.admin.model.form.authorizationinvalidation.AuthorizationInvalidationOutboxRetryForm;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxRetryResultVO;
import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationOutboxProcessor;
import com.auth.service.system.authorization.outbox.AuthorizationInvalidationOutboxStatus;
import com.auth.service.system.authorization.outbox.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.outbox.model.SysAuthorizationInvalidationOutboxEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.feign.AuthInnerFeignClient;

import cn.hutool.core.util.BooleanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Outbox 人工重试
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class AuthorizationInvalidationOutboxManualRetryService {

	private static final Set<String> RESET_REQUIRED_STATUSES = Set.of(AuthorizationInvalidationOutboxStatus.DEAD.name(),
			AuthorizationInvalidationOutboxStatus.FAILED.name(),
			AuthorizationInvalidationOutboxStatus.PROCESSING.name());

	private final SysAuthorizationInvalidationOutboxMapper outboxMapper;

	private final AuthorizationInvalidationOutboxProcessor outboxProcessor;

	private final AuthInnerFeignClient authInnerFeignClient;

	private final AuthorizationInvalidationProperties properties;

	/**
	 * 人工重试单条 Outbox
	 * @param outboxId Outbox 主键
	 * @param form 重试参数
	 * @return 重试结果摘要
	 */
	@Transactional(rollbackFor = Exception.class)
	public AuthorizationInvalidationOutboxRetryResultVO retryById(Long outboxId,
			AuthorizationInvalidationOutboxRetryForm form) {
		// 查询 Outbox 行
		SysAuthorizationInvalidationOutboxEntity row = outboxMapper.selectById(outboxId);
		if (row == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		// 组装重试参数
		AuthorizationInvalidationOutboxRetryForm safeForm = form != null ? form
				: new AuthorizationInvalidationOutboxRetryForm();
		String previousStatus = row.getStatus();
		// 校验是否允许人工重试
		validateRetryable(row, safeForm);

		// 释放 auth 侧 processing 占位（若存在）
		boolean claimReleased = releaseEventClaimIfPresent(row.getEventId());
		// 重置 Outbox 为待投递
		if (RESET_REQUIRED_STATUSES.contains(previousStatus)) {
			resetForManualRetry(row);
		}

		// 投递 Outbox
		boolean dispatched = outboxProcessor.processById(outboxId);
		SysAuthorizationInvalidationOutboxEntity latest = outboxMapper.selectById(outboxId);

		// 组装重试结果
		AuthorizationInvalidationOutboxRetryResultVO resultVO = new AuthorizationInvalidationOutboxRetryResultVO();
		resultVO.setOutboxId(outboxId);
		resultVO.setEventId(row.getEventId());
		resultVO.setPreviousStatus(previousStatus);
		resultVO.setDispatched(dispatched);
		resultVO.setClaimReleased(claimReleased);
		if (latest != null) {
			resultVO.setCurrentStatus(latest.getStatus());
			resultVO.setLastError(latest.getLastError());
		}
		return resultVO;
	}

	/**
	 * 校验是否允许人工重试
	 * @param row Outbox 行
	 * @param form 重试参数
	 */
	private void validateRetryable(SysAuthorizationInvalidationOutboxEntity row,
			AuthorizationInvalidationOutboxRetryForm form) {
		// 校验 Outbox 状态
		String status = row.getStatus();
		if (AuthorizationInvalidationOutboxStatus.SUCCESS.name().equals(status)) {
			throw new SystemBusinessException(AUTH_INVALIDATION_OUTBOX_RETRY_NOT_ALLOWED, status);
		}
		if (!AuthorizationInvalidationOutboxStatus.PROCESSING.name().equals(status)) {
			return;
		}
		if (BooleanUtil.isTrue(form.getForce())) {
			return;
		}
		if (isProcessingUnlockAllowed(row.getLockedAt())) {
			return;
		}
		throw new SystemBusinessException(AUTH_INVALIDATION_OUTBOX_PROCESSING_LOCKED);
	}

	/**
	 * PROCESSING 是否已超过抢占解锁阈值
	 * @param lockedAt 抢占时间
	 * @return 允许解锁时为 true
	 */
	private boolean isProcessingUnlockAllowed(LocalDateTime lockedAt) {
		if (lockedAt == null) {
			return true;
		}

		// 计算解锁阈值
		long timeoutMinutes = properties.getManualProcessingUnlockTimeoutMinutes();
		return !lockedAt.plusMinutes(timeoutMinutes).isAfter(LocalDateTime.now());
	}

	/**
	 * 重置 Outbox 为待投递
	 * @param row 当前行
	 */
	private void resetForManualRetry(SysAuthorizationInvalidationOutboxEntity row) {
		int updated = outboxMapper.resetForManualRetry(row.getId(), row.getVersion());
		if (updated == 0) {
			throw new SystemBusinessException(AUTH_INVALIDATION_OUTBOX_RETRY_CONFLICT);
		}
	}

	/**
	 * 释放 auth 侧 processing 占位（若存在）
	 * @param eventId 业务事件 ID
	 * @return 是否执行了释放
	 */
	private boolean releaseEventClaimIfPresent(String eventId) {
		Result<Boolean> result = authInnerFeignClient.releaseInvalidationEventClaim(eventId);
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Release invalidation event claim unavailable, eventId={}", eventId);
			return false;
		}
		return BooleanUtil.isTrue(result.getData());
	}

}
