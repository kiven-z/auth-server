package com.auth.service.auth.application.port.invalidation;

import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventDetailRowPO;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.auth.domain.authorization.model.query.AuthorizationInvalidationEventQuery;
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

	/**
	 * 统计幂等事件处理状态分布
	 * @return 统计结果
	 */
	AuthorizationInvalidationEventStatsPO selectEventStats();

}
