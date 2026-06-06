package com.auth.service.system.admin.convert;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventStatsVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxStatsVO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventStatsInnerDTO;

/**
 * {@link AuthorizationInvalidationStatsAdminConverter} 单元测试
 */
@DisplayName("AuthorizationInvalidationStatsAdminConverter 运维统计映射")
class AuthorizationInvalidationStatsAdminConverterTest {

	@Test
	@DisplayName("Outbox 统计：null PO 应归零")
	void toOutboxStatsVo_nullPo() {
		AuthorizationInvalidationOutboxStatsVO statsVo = AuthorizationInvalidationStatsAdminConverter
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

		AuthorizationInvalidationOutboxStatsVO statsVo = AuthorizationInvalidationStatsAdminConverter
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

		AuthorizationInvalidationOutboxStatsVO statsVo = AuthorizationInvalidationStatsAdminConverter
			.toOutboxStatsVo(statsPo);

		assertThat(statsVo.getFailureRatePercent()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("幂等事件统计：null DTO 应归零")
	void toEventStatsVo_nullDto() {
		AuthorizationInvalidationEventStatsVO statsVo = AuthorizationInvalidationStatsAdminConverter
			.toEventStatsVo(null);

		assertThat(statsVo.getTotalCount()).isZero();
		assertThat(statsVo.getProcessingCount()).isZero();
		assertThat(statsVo.getCompletedCount()).isZero();
	}

	@Test
	@DisplayName("Outbox 统计：计数字段为空时应归零")
	void toOutboxStatsVo_shouldDefaultNullCountsToZero() {
		AuthorizationInvalidationOutboxStatsVO statsVo = AuthorizationInvalidationStatsAdminConverter
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
		AuthorizationInvalidationEventStatsVO statsVo = AuthorizationInvalidationStatsAdminConverter
			.toEventStatsVo(new AuthorizationInvalidationEventStatsInnerDTO());

		assertThat(statsVo.getTotalCount()).isZero();
		assertThat(statsVo.getProcessingCount()).isZero();
		assertThat(statsVo.getCompletedCount()).isZero();
	}

	@Test
	@DisplayName("幂等事件统计：应映射计数字段")
	void toEventStatsVo_shouldMapCounts() {
		AuthorizationInvalidationEventStatsInnerDTO statsDto = new AuthorizationInvalidationEventStatsInnerDTO();
		statsDto.setTotalCount(8L);
		statsDto.setProcessingCount(1L);
		statsDto.setCompletedCount(7L);

		AuthorizationInvalidationEventStatsVO statsVo = AuthorizationInvalidationStatsAdminConverter
			.toEventStatsVo(statsDto);

		assertThat(statsVo.getTotalCount()).isEqualTo(8L);
		assertThat(statsVo.getProcessingCount()).isEqualTo(1L);
		assertThat(statsVo.getCompletedCount()).isEqualTo(7L);
	}

}
