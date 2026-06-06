package com.auth.service.system.admin.service.impl;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.admin.convert.AuthorizationInvalidationEventAdminConverter;
import com.auth.service.system.admin.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventPageVO;
import com.auth.service.system.admin.service.AuthorizationInvalidationEventAdminService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.feign.AuthInnerFeignClient;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventInnerQuery;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.common.service.AuditUserDisplayService;

import lombok.extern.slf4j.Slf4j;

/**
 * 授权失效幂等事件管理端查询服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationEventAdminServiceImpl implements AuthorizationInvalidationEventAdminService {

	private final AuthInnerFeignClient authInnerFeignClient;

	private final AuditUserDisplayService auditUserDisplayService;

	public AuthorizationInvalidationEventAdminServiceImpl(AuthInnerFeignClient authInnerFeignClient,
			AuditUserDisplayService auditUserDisplayService) {
		this.authInnerFeignClient = authInnerFeignClient;
		this.auditUserDisplayService = auditUserDisplayService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<AuthorizationInvalidationEventPageVO> getPage(AuthorizationInvalidationEventQuery query) {
		// 转换为内部查询对象
		AuthorizationInvalidationEventInnerQuery innerQuery = AuthorizationInvalidationEventAdminConverter.INSTANCE
			.toInnerQuery(query);
		Result<PageResponse<AuthorizationInvalidationEventPageInnerDTO>> result = authInnerFeignClient
			.getInvalidationEventPage(innerQuery);

		// 判断是否成功
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event page unavailable");
			throw new SystemBusinessException(SERVICE_UNAVAILABLE);
		}

		// 转换为VO对象
		PageResponse<AuthorizationInvalidationEventPageInnerDTO> pageData = result.getData();
		List<AuthorizationInvalidationEventPageVO> records = pageData.getList()
			.stream()
			.map(AuthorizationInvalidationEventAdminConverter.INSTANCE::toPageVO)
			.toList();
		PageResponse<AuthorizationInvalidationEventPageVO> response = PageResponse.of(pageData.getPageNo(),
				pageData.getPageSize(), pageData.getTotal(), records);

		// 补充审计用户名
		auditUserDisplayService.enrichAuditUsernames(response.getList());
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationEventDetailVO getDetail(Long id) {
		// 转换为内部查询对象
		Result<AuthorizationInvalidationEventDetailInnerDTO> result = authInnerFeignClient
			.getInvalidationEventDetail(id);

		// 判断是否成功
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event detail unavailable");
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}

		// 转换为VO对象
		AuthorizationInvalidationEventDetailVO detailVO = AuthorizationInvalidationEventAdminConverter.INSTANCE
			.toDetailVO(result.getData());

		// 补充审计用户名
		auditUserDisplayService.enrichAuditUsernames(List.of(detailVO));
		return detailVO;
	}

}
