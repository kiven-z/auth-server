package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.AuthorizationInvalidationOutboxAdminConverter;
import com.auth.service.system.admin.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.admin.service.AuthorizationInvalidationOutboxAdminService;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationOutboxOpsQuery;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationOutboxOpsService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * 授权失效 Outbox 管理端查询服务实现
 *
 * @author Bunny
 */
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationOutboxAdminServiceImpl implements AuthorizationInvalidationOutboxAdminService {

	private final AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	private final AuditUserDisplayService auditUserDisplayService;

	public AuthorizationInvalidationOutboxAdminServiceImpl(
			AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService,
			AuditUserDisplayService auditUserDisplayService) {
		this.authorizationInvalidationOutboxOpsService = authorizationInvalidationOutboxOpsService;
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<AuthorizationInvalidationOutboxPageVO> getPage(AuthorizationInvalidationOutboxQuery query) {
		AuthorizationInvalidationOutboxOpsQuery opsQuery = AuthorizationInvalidationOutboxAdminConverter.INSTANCE
			.toOpsQuery(query);
		IPage<AuthorizationInvalidationOutboxPageRowPO> page = authorizationInvalidationOutboxOpsService
			.getPage(opsQuery);
		IPage<AuthorizationInvalidationOutboxPageVO> voPage = page
			.convert(AuthorizationInvalidationOutboxAdminConverter.INSTANCE::toPageVO);
		auditUserDisplayService.enrichAuditUsernames(voPage);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxDetailVO getDetail(Long id) {
		AuthorizationInvalidationOutboxDetailRowPO detailRow = authorizationInvalidationOutboxOpsService.getDetail(id);
		if (detailRow == null) {
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		AuthorizationInvalidationOutboxDetailVO detailVO = AuthorizationInvalidationOutboxAdminConverter.INSTANCE
			.toDetailVo(detailRow);
		auditUserDisplayService.enrichAuditUsernames(List.of(detailVO));
		return detailVO;
	}

}
