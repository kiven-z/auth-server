package com.auth.service.system.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.AuthorizationInvalidationEventAdminConverter;
import com.auth.service.system.admin.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventPageVO;
import com.auth.service.system.admin.service.AuthorizationInvalidationEventAdminService;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationEventOpsQuery;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.common.service.AuditUserDisplayService;

import lombok.RequiredArgsConstructor;

/**
 * 授权失效幂等事件管理端查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationEventAdminServiceImpl implements AuthorizationInvalidationEventAdminService {

	private final AuthorizationInvalidationEventOpsService authorizationInvalidationEventOpsService;

	private final AuditUserDisplayService auditUserDisplayService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<AuthorizationInvalidationEventPageVO> getPage(AuthorizationInvalidationEventQuery query) {
		AuthorizationInvalidationEventOpsQuery opsQuery = AuthorizationInvalidationEventAdminConverter.INSTANCE
			.toOpsQuery(query);
		PageResponse<AuthorizationInvalidationEventPageInnerDTO> pageData = authorizationInvalidationEventOpsService
			.getPage(opsQuery);

		List<AuthorizationInvalidationEventPageVO> records = pageData.getList()
			.stream()
			.map(AuthorizationInvalidationEventAdminConverter.INSTANCE::toPageVO)
			.toList();
		PageResponse<AuthorizationInvalidationEventPageVO> response = PageResponse.of(pageData.getPageNo(),
				pageData.getPageSize(), pageData.getTotal(), records);

		auditUserDisplayService.enrichAuditUsernames(response.getList());
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationEventDetailVO getDetail(Long id) {
		AuthorizationInvalidationEventDetailInnerDTO detailDto = authorizationInvalidationEventOpsService.getDetail(id);
		AuthorizationInvalidationEventDetailVO detailVO = AuthorizationInvalidationEventAdminConverter.INSTANCE
			.toDetailVO(detailDto);

		auditUserDisplayService.enrichAuditUsernames(List.of(detailVO));
		return detailVO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean releaseProcessingClaim(Long id) {
		return authorizationInvalidationEventOpsService.releaseProcessingClaim(id);
	}

}
