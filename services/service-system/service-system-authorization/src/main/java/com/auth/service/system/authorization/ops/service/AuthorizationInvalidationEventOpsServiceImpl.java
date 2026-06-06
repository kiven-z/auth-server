package com.auth.service.system.authorization.ops.service;

import cn.hutool.core.util.BooleanUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationEventOpsQuery;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.feign.AuthInnerFeignClient;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventInnerQuery;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventStatsInnerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auth.service.system.authorization.ops.exception.AuthorizationInvalidationOpsResultCode.EVENT_RELEASE_NOT_PROCESSING;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;

/**
 * 授权失效幂等事件运维门面实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthorizationInvalidationEventOpsServiceImpl implements AuthorizationInvalidationEventOpsService {

	private final AuthInnerFeignClient authInnerFeignClient;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<AuthorizationInvalidationEventPageInnerDTO> getPage(
			AuthorizationInvalidationEventOpsQuery query) {
		Result<PageResponse<AuthorizationInvalidationEventPageInnerDTO>> result = authInnerFeignClient
			.getInvalidationEventPage(toInnerQuery(query));
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event page unavailable");
			throw new SystemBusinessException(SERVICE_UNAVAILABLE);
		}
		return result.getData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationEventDetailInnerDTO getDetail(Long id) {
		Result<AuthorizationInvalidationEventDetailInnerDTO> result = authInnerFeignClient
			.getInvalidationEventDetail(id);
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event detail unavailable, id={}", id);
			throw new SystemBusinessException(DATA_NOT_EXIST);
		}
		return result.getData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationInvalidationEventStatsPO getEventStats() {
		Result<AuthorizationInvalidationEventStatsInnerDTO> result = authInnerFeignClient.getInvalidationEventSummary();
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event summary unavailable");
			throw new SystemBusinessException(SERVICE_UNAVAILABLE);
		}
		return toStatsPo(result.getData());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean releaseProcessingClaim(Long id) {
		AuthorizationInvalidationEventDetailInnerDTO detail = getDetail(id);
		if (BooleanUtil.isFalse(detail.getProcessing())) {
			throw new SystemBusinessException(EVENT_RELEASE_NOT_PROCESSING);
		}

		Result<Boolean> result = authInnerFeignClient.releaseInvalidationEventClaim(detail.getEventId());
		if (!FeignUtil.isSuccessWithData(result)) {
			log.warn("Authorization invalidation event release claim unavailable, id={}", id);
			throw new SystemBusinessException(SERVICE_UNAVAILABLE);
		}
		return BooleanUtil.isTrue(result.getData());
	}

	private AuthorizationInvalidationEventInnerQuery toInnerQuery(AuthorizationInvalidationEventOpsQuery query) {
		AuthorizationInvalidationEventInnerQuery innerQuery = new AuthorizationInvalidationEventInnerQuery();
		innerQuery.setPageIndex(Math.toIntExact(query.getPageIndex()));
		innerQuery.setPageSize(Math.toIntExact(query.getPageSize()));
		innerQuery.setEventId(query.getEventId());
		innerQuery.setChangeKind(query.getChangeKind());
		innerQuery.setProcessing(query.getProcessing());
		innerQuery.setProcessedAtStart(query.getProcessedAtStart());
		innerQuery.setProcessedAtEnd(query.getProcessedAtEnd());
		return innerQuery;
	}

	private AuthorizationInvalidationEventStatsPO toStatsPo(AuthorizationInvalidationEventStatsInnerDTO statsDto) {
		AuthorizationInvalidationEventStatsPO statsPo = new AuthorizationInvalidationEventStatsPO();
		statsPo.setTotalCount(statsDto.getTotalCount());
		statsPo.setProcessingCount(statsDto.getProcessingCount());
		statsPo.setCompletedCount(statsDto.getCompletedCount());
		return statsPo;
	}

}
