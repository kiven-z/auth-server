package com.auth.service.system.admin.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.service.system.admin.convert.AuthorizationInvalidationStatsAdminConverter;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationSummaryVO;
import com.auth.service.system.admin.service.AuthorizationInvalidationStatsAdminService;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationOutboxOpsService;

import lombok.RequiredArgsConstructor;

/**
 * 授权失效运维统计服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationStatsAdminServiceImpl implements AuthorizationInvalidationStatsAdminService {

	private final AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	private final AuthorizationInvalidationEventOpsService authorizationInvalidationEventOpsService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationSummaryVO getSummary() {
		AuthorizationInvalidationSummaryVO summaryVO = new AuthorizationInvalidationSummaryVO();
		summaryVO.setOutbox(AuthorizationInvalidationStatsAdminConverter
			.toOutboxStatsVo(authorizationInvalidationOutboxOpsService.getOutboxStats()));
		summaryVO.setEvent(AuthorizationInvalidationStatsAdminConverter
			.toEventStatsVo(authorizationInvalidationEventOpsService.getEventStats()));
		return summaryVO;
	}

}
