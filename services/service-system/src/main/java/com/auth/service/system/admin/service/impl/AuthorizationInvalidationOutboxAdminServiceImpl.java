package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.service.AuthorizationInvalidationOutboxAdminService;
import com.auth.service.system.authorization.application.query.AuthorizationInvalidationOutboxQueryApplicationService;
import com.auth.service.system.authorization.outbox.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.outbox.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.outbox.model.vo.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.common.service.AuditUserDisplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 授权失效 Outbox 管理端查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationOutboxAdminServiceImpl implements AuthorizationInvalidationOutboxAdminService {

	private final AuthorizationInvalidationOutboxQueryApplicationService queryApplicationService;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<AuthorizationInvalidationOutboxPageVO> getPage(AuthorizationInvalidationOutboxQuery query) {
		PageResponse<AuthorizationInvalidationOutboxPageVO> response = queryApplicationService.getPage(query);
		auditUserDisplayService.enrichAuditUsernames(response.getList());
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationOutboxDetailVO getDetail(Long id) {
		AuthorizationInvalidationOutboxDetailVO detailVO = queryApplicationService.getDetail(id);
		auditUserDisplayService.enrichAuditUsernames(List.of(detailVO));
		return detailVO;
	}

}
