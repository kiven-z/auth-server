package com.auth.service.system.authorization.outbox.port;

import com.auth.service.system.authorization.outbox.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.outbox.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.outbox.model.query.AuthorizationInvalidationOutboxQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 授权失效 Outbox 只读查询端口
 *
 * @author Bunny
 */
public interface InvalidationOutboxQueryPort {

	/**
	 * 分页查询 Outbox
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<AuthorizationInvalidationOutboxPageRowPO> selectPage(Page<AuthorizationInvalidationOutboxPageRowPO> page,
			AuthorizationInvalidationOutboxQuery query);

	/**
	 * 按主键查询详情
	 * @param id 主键
	 * @return 详情行，不存在时为 null
	 */
	AuthorizationInvalidationOutboxDetailRowPO selectDetailById(Long id);

}
