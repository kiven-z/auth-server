package com.auth.service.system.authorization.ops.manualretry;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.authorization.model.record.AuthorizationInvalidationOutboxManualRetryOutcome;
import com.auth.service.system.authorization.model.form.AuthorizationInvalidationOutboxRetryForm;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxRetryResultVO;
import com.auth.service.system.authorization.service.AuthorizationInvalidationOutboxOpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Outbox 人工重试运维门面
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
