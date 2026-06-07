package com.auth.service.system.authorization.ops.service;

import com.auth.service.system.authorization.ops.manualretry.AuthorizationInvalidationOutboxManualRetryApplicationService;
import com.auth.service.system.authorization.ops.mapper.AuthorizationInvalidationOutboxOpsMapper;
import com.auth.service.system.authorization.ops.model.AuthorizationInvalidationOutboxManualRetryOutcome;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationOutboxOpsQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 授权失效 Outbox 运维门面实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationOutboxOpsServiceImpl implements AuthorizationInvalidationOutboxOpsService {

	private final AuthorizationInvalidationOutboxOpsMapper authorizationInvalidationOutboxOpsMapper;

	private final AuthorizationInvalidationOutboxManualRetryApplicationService manualRetryApplicationService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPage<AuthorizationInvalidationOutboxPageRowPO> getPage(AuthorizationInvalidationOutboxOpsQuery query) {
		Page<AuthorizationInvalidationOutboxPageRowPO> pageParams = new Page<>(query.getPageIndex(),
				query.getPageSize());
		return authorizationInvalidationOutboxOpsMapper.selectListByPage(pageParams, query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxDetailRowPO getDetail(Long id) {
		return authorizationInvalidationOutboxOpsMapper.selectDetailById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxStatsPO getOutboxStats() {
		return authorizationInvalidationOutboxOpsMapper.selectOutboxStats();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxManualRetryOutcome retryManual(Long outboxId, boolean force) {
		return manualRetryApplicationService.retryManual(outboxId, force);
	}

}
