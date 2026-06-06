package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventPageVO;

/**
 * 授权失效幂等事件管理端查询服务
 *
 * @author Bunny
 */
public interface AuthorizationInvalidationEventAdminService {

	/**
	 * 分页查询幂等事件
	 * @param query 查询条件
	 * @return 分页结果
	 */
	PageResponse<AuthorizationInvalidationEventPageVO> getPage(AuthorizationInvalidationEventQuery query);

	/**
	 * 查询幂等事件详情
	 * @param id 主键
	 * @return 详情
	 */
	AuthorizationInvalidationEventDetailVO getDetail(Long id);

}
