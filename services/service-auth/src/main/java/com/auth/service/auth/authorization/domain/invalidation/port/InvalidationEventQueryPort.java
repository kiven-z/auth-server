package com.auth.service.auth.authorization.domain.invalidation.port;

import com.auth.service.auth.authorization.domain.model.po.AuthorizationInvalidationEventDetailRowPO;
import com.auth.service.auth.authorization.domain.model.po.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.authorization.domain.model.query.AuthorizationInvalidationEventQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 授权失效幂等事件只读查询端口
 *
 * @author Bunny
 */
public interface InvalidationEventQueryPort {

	/**
	 * 分页查询幂等事件
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<AuthorizationInvalidationEventPageRowPO> selectPage(Page<AuthorizationInvalidationEventPageRowPO> page,
			AuthorizationInvalidationEventQuery query);

	/**
	 * 按主键查询详情
	 * @param id 主键
	 * @return 详情投影，不存在时为 null
	 */
	AuthorizationInvalidationEventDetailRowPO selectDetailById(Long id);

}
