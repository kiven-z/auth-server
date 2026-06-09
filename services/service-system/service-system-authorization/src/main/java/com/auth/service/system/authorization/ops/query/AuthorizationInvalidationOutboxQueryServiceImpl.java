package com.auth.service.system.authorization.ops.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.ops.convert.AuthorizationInvalidationOutboxOpsConverter;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationOutboxOpsQuery;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationOutboxOpsService;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.RequiredArgsConstructor;

/**
 * 授权失效 Outbox 运维查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationOutboxQueryServiceImpl implements AuthorizationInvalidationOutboxQueryService {

	private final AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<AuthorizationInvalidationOutboxPageVO> getPage(AuthorizationInvalidationOutboxQuery query) {
		AuthorizationInvalidationOutboxOpsQuery opsQuery = AuthorizationInvalidationOutboxOpsConverter.INSTANCE
			.toOpsQuery(query);
		IPage<AuthorizationInvalidationOutboxPageRowPO> page = authorizationInvalidationOutboxOpsService
			.getPage(opsQuery);
		IPage<AuthorizationInvalidationOutboxPageVO> voPage = page
			.convert(AuthorizationInvalidationOutboxOpsConverter.INSTANCE::toPageVO);
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxDetailVO getDetail(Long id) {
		AuthorizationInvalidationOutboxDetailRowPO detailRow = authorizationInvalidationOutboxOpsService.getDetail(id);
		AuthorizationInvalidationOutboxDetailVO detailVO = AuthorizationInvalidationOutboxOpsConverter.INSTANCE
			.toDetailVo(detailRow);
		auditUserDisplayService.enrichAuditUsernames(List.of(detailVO));
		return detailVO;
	}

}
