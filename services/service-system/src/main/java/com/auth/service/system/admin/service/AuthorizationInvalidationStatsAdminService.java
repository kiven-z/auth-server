package com.auth.service.system.admin.service;

import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationSummaryVO;

/**
 * 授权失效运维统计服务
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationStatsAdminService {

	/**
	 * 查询 Outbox 与幂等事件统计摘要
	 * @return 统计摘要
	 */
	AuthorizationInvalidationSummaryVO getSummary();

}
