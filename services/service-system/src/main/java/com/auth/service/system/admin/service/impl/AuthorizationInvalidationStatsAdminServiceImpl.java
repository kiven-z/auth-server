package com.auth.service.system.admin.service.impl;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.admin.convert.AuthorizationInvalidationStatsAdminConverter;
import com.auth.service.system.admin.mapper.AuthorizationInvalidationOutboxAdminMapper;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventStatsVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationSummaryVO;
import com.auth.service.system.admin.service.AuthorizationInvalidationStatsAdminService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.feign.AuthInnerFeignClient;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventStatsInnerDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * 授权失效运维统计服务实现
 *
 * @author Bunny
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationStatsAdminServiceImpl implements AuthorizationInvalidationStatsAdminService {

	private final AuthorizationInvalidationOutboxAdminMapper authorizationInvalidationOutboxAdminMapper;

	private final AuthInnerFeignClient authInnerFeignClient;

	public AuthorizationInvalidationStatsAdminServiceImpl(
			AuthorizationInvalidationOutboxAdminMapper authorizationInvalidationOutboxAdminMapper,
			AuthInnerFeignClient authInnerFeignClient) {
		this.authorizationInvalidationOutboxAdminMapper = authorizationInvalidationOutboxAdminMapper;
		this.authInnerFeignClient = authInnerFeignClient;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationSummaryVO getSummary() {
		AuthorizationInvalidationSummaryVO summaryVO = new AuthorizationInvalidationSummaryVO();
		summaryVO.setOutbox(AuthorizationInvalidationStatsAdminConverter
			.toOutboxStatsVo(authorizationInvalidationOutboxAdminMapper.selectOutboxStats()));
		summaryVO.setEvent(loadEventStats());
		return summaryVO;
	}

	/**
	 * 经 Feign 拉取幂等事件统计
	 * @return 事件统计 VO
	 */
	private AuthorizationInvalidationEventStatsVO loadEventStats() {
		Result<AuthorizationInvalidationEventStatsInnerDTO> result = authInnerFeignClient.getInvalidationEventSummary();
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event summary unavailable");
			throw new SystemBusinessException(SERVICE_UNAVAILABLE);
		}
		return AuthorizationInvalidationStatsAdminConverter.toEventStatsVo(result.getData());
	}

}
