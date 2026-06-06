package com.auth.service.system.admin.service.impl;

import org.springframework.stereotype.Service;

import com.auth.service.system.admin.model.form.authorizationinvalidation.AuthorizationInvalidationOutboxRetryForm;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxRetryResultVO;
import com.auth.service.system.authorization.ops.model.AuthorizationInvalidationOutboxManualRetryOutcome;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationOutboxOpsService;

import cn.hutool.core.util.BooleanUtil;
import lombok.RequiredArgsConstructor;

/**
 * Outbox 人工重试管理端门面
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class AuthorizationInvalidationOutboxManualRetryService {

	private final AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	/**
	 * 人工重试单条 Outbox
	 * @param outboxId Outbox 主键
	 * @param form 重试参数
	 * @return 重试结果摘要
	 */
	public AuthorizationInvalidationOutboxRetryResultVO retryById(Long outboxId,
			AuthorizationInvalidationOutboxRetryForm form) {
		boolean force = form != null && BooleanUtil.isTrue(form.getForce());
		AuthorizationInvalidationOutboxManualRetryOutcome outcome = authorizationInvalidationOutboxOpsService
			.retryManual(outboxId, force);
		AuthorizationInvalidationOutboxRetryResultVO resultVO = new AuthorizationInvalidationOutboxRetryResultVO();
		resultVO.setOutboxId(outcome.outboxId());
		resultVO.setEventId(outcome.eventId());
		resultVO.setPreviousStatus(outcome.previousStatus());
		resultVO.setCurrentStatus(outcome.currentStatus());
		resultVO.setLastError(outcome.lastError());
		resultVO.setDispatched(outcome.dispatched());
		resultVO.setClaimReleased(outcome.claimReleased());
		return resultVO;
	}

}
