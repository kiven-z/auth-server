package com.auth.service.system.authorization.service;

import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationSummaryVO;

/**
 * 授权失效运维统计查询服务
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationStatsQueryService {

	/**
	 * 查询 Outbox 与幂等事件统计摘要
	 * @return 统计摘要
	 */
	AuthorizationInvalidationSummaryVO getSummary();

}
