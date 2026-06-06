package com.auth.service.system.admin.convert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventStatsVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxStatsVO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventStatsInnerDTO;

import lombok.experimental.UtilityClass;

/**
 * 授权失效运维统计转换器；含 null 默认值与失败率计算，不使用 MapStruct。
 *
 * @author Bunny
 */
@UtilityClass
public class AuthorizationInvalidationStatsAdminConverter {

	private static final int FAILURE_RATE_SCALE = 2;

	/**
	 * Outbox 统计 PO 转 VO
	 * @param statsPo 统计 PO
	 * @return Outbox 统计 VO
	 */
	public static AuthorizationInvalidationOutboxStatsVO toOutboxStatsVo(
			AuthorizationInvalidationOutboxStatsPO statsPo) {
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

		long total = Objects.requireNonNullElse(statsPo.getTotalCount(), 0L);
		long failed = Objects.requireNonNullElse(statsPo.getFailedCount(), 0L);
		long dead = Objects.requireNonNullElse(statsPo.getDeadCount(), 0L);
		statsVo.setTotalCount(total);
		statsVo.setPendingCount(Objects.requireNonNullElse(statsPo.getPendingCount(), 0L));
		statsVo.setProcessingCount(Objects.requireNonNullElse(statsPo.getProcessingCount(), 0L));
		statsVo.setSuccessCount(Objects.requireNonNullElse(statsPo.getSuccessCount(), 0L));
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
	public static AuthorizationInvalidationEventStatsVO toEventStatsVo(
			AuthorizationInvalidationEventStatsInnerDTO statsDto) {
		AuthorizationInvalidationEventStatsVO statsVo = new AuthorizationInvalidationEventStatsVO();
		if (statsDto == null) {
			statsVo.setTotalCount(0L);
			statsVo.setProcessingCount(0L);
			statsVo.setCompletedCount(0L);
			return statsVo;
		}
		statsVo.setTotalCount(Objects.requireNonNullElse(statsDto.getTotalCount(), 0L));
		statsVo.setProcessingCount(Objects.requireNonNullElse(statsDto.getProcessingCount(), 0L));
		statsVo.setCompletedCount(Objects.requireNonNullElse(statsDto.getCompletedCount(), 0L));
		return statsVo;
	}

	/**
	 * 计算失败率百分比
	 * @param total 总记录数
	 * @param failed 待重试记录数
	 * @param dead 死信记录数
	 * @return 失败率百分比
	 */
	private static BigDecimal calculateFailureRatePercent(long total, long failed, long dead) {
		if (total <= 0L) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(failed + dead)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(total), FAILURE_RATE_SCALE, RoundingMode.HALF_UP);
	}

}
