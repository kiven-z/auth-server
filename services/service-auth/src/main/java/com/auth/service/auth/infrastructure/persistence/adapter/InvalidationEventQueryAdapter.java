package com.auth.service.auth.infrastructure.persistence.adapter;

import com.auth.service.auth.application.port.invalidation.InvalidationEventQueryPort;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventDetailRowPO;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.auth.domain.authorization.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.infrastructure.persistence.AuthorizationInvalidationEventMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

/**
 * 授权失效幂等事件只读查询适配器
 *
 * @author Bunny
 */
@Repository
public class InvalidationEventQueryAdapter implements InvalidationEventQueryPort {

	private final AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper;

	public InvalidationEventQueryAdapter(AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper) {
		this.authorizationInvalidationEventMapper = authorizationInvalidationEventMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPage<AuthorizationInvalidationEventPageRowPO> selectPage(Page<AuthorizationInvalidationEventPageRowPO> page,
			AuthorizationInvalidationEventQuery query) {
		return authorizationInvalidationEventMapper.selectListByPage(page, query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationEventDetailRowPO selectDetailById(Long id) {
		return authorizationInvalidationEventMapper.selectDetailById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationEventStatsPO selectEventStats() {
		return authorizationInvalidationEventMapper.selectEventStats();
	}

}
