package com.auth.service.auth.convert;

import com.auth.service.auth.model.po.invalidation.AuthorizationInvalidationEventStatsPO;
import com.auth.service.auth.model.vo.authorization.AuthorizationInvalidationEventStatsVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AuthorizationInvalidationEventStatsConverter} 单元测试
 */
@DisplayName("AuthorizationInvalidationEventStatsConverter 统计 PO→VO")
class AuthorizationInvalidationEventStatsConverterTest {

	@Test
	@DisplayName("null PO 时各计数归零")
	void toVo_shouldDefaultNullPoToZero() {
		AuthorizationInvalidationEventStatsVO statsVo = AuthorizationInvalidationEventStatsConverter.toVo(null);

		assertEquals(0L, statsVo.getTotalCount());
		assertEquals(0L, statsVo.getProcessingCount());
		assertEquals(0L, statsVo.getCompletedCount());
	}

	@Test
	@DisplayName("计数字段为空时归零")
	void toVo_shouldDefaultNullCountsToZero() {
		AuthorizationInvalidationEventStatsVO statsVo = AuthorizationInvalidationEventStatsConverter
			.toVo(new AuthorizationInvalidationEventStatsPO());

		assertEquals(0L, statsVo.getTotalCount());
		assertEquals(0L, statsVo.getProcessingCount());
		assertEquals(0L, statsVo.getCompletedCount());
	}

	@Test
	@DisplayName("应映射统计计数字段")
	void toVo_shouldMapCounts() {
		AuthorizationInvalidationEventStatsPO statsPo = new AuthorizationInvalidationEventStatsPO();
		statsPo.setTotalCount(12L);
		statsPo.setProcessingCount(1L);
		statsPo.setCompletedCount(11L);

		AuthorizationInvalidationEventStatsVO statsVo = AuthorizationInvalidationEventStatsConverter.toVo(statsPo);

		assertEquals(12L, statsVo.getTotalCount());
		assertEquals(1L, statsVo.getProcessingCount());
		assertEquals(11L, statsVo.getCompletedCount());
	}

}
