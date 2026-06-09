package com.auth.service.system.authorization.ops.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.service.system.authorization.ops.convert.AuthorizationInvalidationStatsOpsConverter;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationSummaryVO;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationOutboxOpsService;

import lombok.RequiredArgsConstructor;

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
