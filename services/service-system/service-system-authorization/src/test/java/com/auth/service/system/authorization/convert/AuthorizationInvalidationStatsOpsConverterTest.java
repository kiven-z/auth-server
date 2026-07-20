package com.auth.service.system.authorization.convert;

import com.auth.service.system.authorization.feign.dto.AuthorizationInvalidationEventStatsInnerDTO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationFailureRateTrendBucketPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationEventStatsVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationFailureRateTrendPointVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxStatsVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AuthorizationInvalidationStatsOpsConverter} 单元测试
 */
@DisplayName("AuthorizationInvalidationStatsOpsConverter 运维统计映射")
class AuthorizationInvalidationStatsOpsConverterTest {

	@Test
	@DisplayName("Outbox 统计：null PO 应归零")
	void toOutboxStatsVo_nullPo() {
		AuthorizationInvalidationOutboxStatsVO statsVo = AuthorizationInvalidationStatsOpsConverter
			.toOutboxStatsVo(null);

		assertThat(statsVo.getTotalCount()).isZero();
		assertThat(statsVo.getPendingCount()).isZero();
		assertThat(statsVo.getProcessingCount()).isZero();
		assertThat(statsVo.getSuccessCount()).isZero();
		assertThat(statsVo.getFailedCount()).isZero();
		assertThat(statsVo.getDeadCount()).isZero();
		assertThat(statsVo.getFailureRatePercent()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("Outbox 统计：应计算失败率百分比")
	void toOutboxStatsVo_shouldCalculateFailureRate() {
		AuthorizationInvalidationOutboxStatsPO statsPo = new AuthorizationInvalidationOutboxStatsPO();
		statsPo.setTotalCount(10L);
		statsPo.setPendingCount(1L);
		statsPo.setProcessingCount(2L);
		statsPo.setSuccessCount(5L);
		statsPo.setFailedCount(1L);
		statsPo.setDeadCount(1L);

		AuthorizationInvalidationOutboxStatsVO statsVo = AuthorizationInvalidationStatsOpsConverter
			.toOutboxStatsVo(statsPo);

		assertThat(statsVo.getTotalCount()).isEqualTo(10L);
		assertThat(statsVo.getDeadCount()).isEqualTo(1L);
		assertThat(statsVo.getFailureRatePercent()).isEqualByComparingTo(new BigDecimal("20.00"));
	}

	@Test
	@DisplayName("Outbox 统计：总数为零时失败率为 0")
	void toOutboxStatsVo_zeroTotal() {
		AuthorizationInvalidationOutboxStatsPO statsPo = new AuthorizationInvalidationOutboxStatsPO();
		statsPo.setTotalCount(0L);
		statsPo.setFailedCount(1L);
		statsPo.setDeadCount(1L);

		AuthorizationInvalidationOutboxStatsVO statsVo = AuthorizationInvalidationStatsOpsConverter
			.toOutboxStatsVo(statsPo);

		assertThat(statsVo.getFailureRatePercent()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("幂等事件统计：null PO 应归零")
	void toEventStatsVo_nullPo() {
		AuthorizationInvalidationEventStatsVO statsVo = AuthorizationInvalidationStatsOpsConverter.toEventStatsVo(null);

		assertThat(statsVo.getTotalCount()).isZero();
		assertThat(statsVo.getProcessingCount()).isZero();
		assertThat(statsVo.getCompletedCount()).isZero();
	}

	@Test
	@DisplayName("Outbox 统计：计数字段为空时应归零")
	void toOutboxStatsVo_shouldDefaultNullCountsToZero() {
		AuthorizationInvalidationOutboxStatsVO statsVo = AuthorizationInvalidationStatsOpsConverter
			.toOutboxStatsVo(new AuthorizationInvalidationOutboxStatsPO());

		assertThat(statsVo.getTotalCount()).isZero();
		assertThat(statsVo.getPendingCount()).isZero();
		assertThat(statsVo.getProcessingCount()).isZero();
		assertThat(statsVo.getSuccessCount()).isZero();
		assertThat(statsVo.getFailedCount()).isZero();
		assertThat(statsVo.getDeadCount()).isZero();
		assertThat(statsVo.getFailureRatePercent()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("幂等事件统计：计数字段为空时应归零")
	void toEventStatsVo_shouldDefaultNullCountsToZero() {
		AuthorizationInvalidationEventStatsVO statsVo = AuthorizationInvalidationStatsOpsConverter
			.toEventStatsVo(new AuthorizationInvalidationEventStatsPO());

		assertThat(statsVo.getTotalCount()).isZero();
		assertThat(statsVo.getProcessingCount()).isZero();
		assertThat(statsVo.getCompletedCount()).isZero();
	}

	@Test
	@DisplayName("Feign 统计摘要应映射为统计 PO")
	void toEventStatsPo_shouldMapCounts() {
		AuthorizationInvalidationEventStatsInnerDTO statsDto = new AuthorizationInvalidationEventStatsInnerDTO();
		statsDto.setTotalCount(8L);
		statsDto.setProcessingCount(1L);
		statsDto.setCompletedCount(7L);

		AuthorizationInvalidationEventStatsPO statsPo = AuthorizationInvalidationStatsOpsConverter
			.toEventStatsPo(statsDto);

		assertThat(statsPo.getTotalCount()).isEqualTo(8L);
		assertThat(statsPo.getProcessingCount()).isEqualTo(1L);
		assertThat(statsPo.getCompletedCount()).isEqualTo(7L);
	}

	@Test
	@DisplayName("幂等事件统计：应映射计数字段")
	void toEventStatsVo_shouldMapCounts() {
		AuthorizationInvalidationEventStatsPO statsPo = new AuthorizationInvalidationEventStatsPO();
		statsPo.setTotalCount(8L);
		statsPo.setProcessingCount(1L);
		statsPo.setCompletedCount(7L);

		AuthorizationInvalidationEventStatsVO statsVo = AuthorizationInvalidationStatsOpsConverter
			.toEventStatsVo(statsPo);

		assertThat(statsVo.getTotalCount()).isEqualTo(8L);
		assertThat(statsVo.getProcessingCount()).isEqualTo(1L);
		assertThat(statsVo.getCompletedCount()).isEqualTo(7L);
	}

	@Test
	@DisplayName("失败率趋势点：应映射分桶并计算失败率")
	void toFailureRateTrendPointVo_shouldMapBucketAndFailureRate() {
		AuthorizationInvalidationFailureRateTrendBucketPO bucketPo = new AuthorizationInvalidationFailureRateTrendBucketPO();
		bucketPo.setBucket("2026-06-01");
		bucketPo.setTotalCount(10L);
		bucketPo.setFailedCount(1L);
		bucketPo.setDeadCount(1L);

		AuthorizationInvalidationFailureRateTrendPointVO pointVo = AuthorizationInvalidationStatsOpsConverter
			.toFailureRateTrendPointVo(bucketPo);

		assertThat(pointVo.getBucket()).isEqualTo("2026-06-01");
		assertThat(pointVo.getTotalCount()).isEqualTo(10L);
		assertThat(pointVo.getFailureRatePercent()).isEqualByComparingTo(new BigDecimal("20.00"));
	}

}
