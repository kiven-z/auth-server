package com.auth.service.system.admin.service.impl;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.service.system.admin.mapper.AuthorizationInvalidationOutboxAdminMapper;
import com.auth.service.system.admin.model.po.authorizationinvalidation.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventStatsVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxStatsVO;
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

	private static final int FAILURE_RATE_SCALE = 2;

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
		summaryVO.setOutbox(toOutboxStatsVo(authorizationInvalidationOutboxAdminMapper.selectOutboxStats()));
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
		return toEventStatsVo(result.getData());
	}

	/**
	 * Outbox 统计 PO 转 VO
	 * @param statsPo 统计 PO
	 * @return Outbox 统计 VO
	 */
	private AuthorizationInvalidationOutboxStatsVO toOutboxStatsVo(AuthorizationInvalidationOutboxStatsPO statsPo) {
		AuthorizationInvalidationOutboxStatsVO statsVo = new AuthorizationInvalidationOutboxStatsVO();
		if (statsPo == null) {
			statsVo.setTotalCount(0L);
			statsVo.setPendingCount(0L);
			statsVo.setProcessingCount(0L);
			statsVo.setSuccessCount(0L);
			statsVo.setFailedCount(0L);
			statsVo.setDeadCount(0L);
			statsVo.setFailureRatePercent(BigDecimal.ZERO);
			return statsVo;
		}

		long total = defaultCount(statsPo.getTotalCount());
		long failed = defaultCount(statsPo.getFailedCount());
		long dead = defaultCount(statsPo.getDeadCount());
		statsVo.setTotalCount(total);
		statsVo.setPendingCount(defaultCount(statsPo.getPendingCount()));
		statsVo.setProcessingCount(defaultCount(statsPo.getProcessingCount()));
		statsVo.setSuccessCount(defaultCount(statsPo.getSuccessCount()));
		statsVo.setFailedCount(failed);
		statsVo.setDeadCount(dead);
		statsVo.setFailureRatePercent(calculateFailureRatePercent(total, failed, dead));
		return statsVo;
	}

	/**
	 * 幂等事件统计 DTO 转 VO
	 * @param statsDto Feign DTO
	 * @return 事件统计 VO
	 */
	private AuthorizationInvalidationEventStatsVO toEventStatsVo(AuthorizationInvalidationEventStatsInnerDTO statsDto) {
		AuthorizationInvalidationEventStatsVO statsVo = new AuthorizationInvalidationEventStatsVO();
		if (statsDto == null) {
			statsVo.setTotalCount(0L);
			statsVo.setProcessingCount(0L);
			statsVo.setCompletedCount(0L);
			return statsVo;
		}
		statsVo.setTotalCount(defaultCount(statsDto.getTotalCount()));
		statsVo.setProcessingCount(defaultCount(statsDto.getProcessingCount()));
		statsVo.setCompletedCount(defaultCount(statsDto.getCompletedCount()));
		return statsVo;
	}

	/**
	 * 计算失败率百分比
	 * @param total 总记录数
	 * @param failed 待重试记录数
	 * @param dead 死信记录数
	 * @return 失败率百分比
	 */
	private BigDecimal calculateFailureRatePercent(long total, long failed, long dead) {
		if (total <= 0L) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(failed + dead)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(total), FAILURE_RATE_SCALE, RoundingMode.HALF_UP);
	}

	/**
	 * 空值计数归零
	 * @param count 原始计数
	 * @return 非空计数
	 */
	private static long defaultCount(Long count) {
		return count != null ? count : 0L;
	}

}
