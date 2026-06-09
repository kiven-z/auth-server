package com.auth.service.system.authorization.service.impl;

import com.auth.service.system.authorization.convert.AuthorizationInvalidationStatsOpsConverter;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationSummaryVO;
import com.auth.service.system.authorization.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.authorization.service.AuthorizationInvalidationOutboxOpsService;
import com.auth.service.system.authorization.service.AuthorizationInvalidationStatsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 授权失效运维统计查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationStatsQueryServiceImpl implements AuthorizationInvalidationStatsQueryService {

	private final AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	private final AuthorizationInvalidationEventOpsService authorizationInvalidationEventOpsService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationSummaryVO getSummary() {
		AuthorizationInvalidationSummaryVO summaryVO = new AuthorizationInvalidationSummaryVO();
		summaryVO.setOutbox(AuthorizationInvalidationStatsOpsConverter
			.toOutboxStatsVo(authorizationInvalidationOutboxOpsService.getOutboxStats()));
		summaryVO.setEvent(AuthorizationInvalidationStatsOpsConverter
			.toEventStatsVo(authorizationInvalidationEventOpsService.getEventStats()));
		return summaryVO;
	}

}
