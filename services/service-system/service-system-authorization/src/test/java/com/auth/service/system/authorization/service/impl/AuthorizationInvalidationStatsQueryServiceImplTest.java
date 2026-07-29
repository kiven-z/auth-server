package com.auth.service.system.authorization.service.impl;

import com.auth.service.system.authorization.mapper.AuthorizationInvalidationOutboxOpsMapper;
import com.auth.service.system.authorization.model.enums.AuthorizationInvalidationFailureRateTrendGranularity;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationFailureRateTrendBucketPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationFailureRateTrendQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationFailureRateTrendVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationSummaryVO;
import com.auth.service.system.authorization.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.authorization.service.AuthorizationInvalidationOutboxOpsService;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationStatsQueryServiceImpl} 单元测试
 */
@DisplayName("AuthorizationInvalidationStatsQueryServiceImpl 运维统计")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationStatsQueryServiceImplTest {

	@Mock
	private AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	@Mock
	private AuthorizationInvalidationEventOpsService authorizationInvalidationEventOpsService;

	@Mock
	private AuthorizationInvalidationOutboxOpsMapper authorizationInvalidationOutboxOpsMapper;

	@InjectMocks
	private AuthorizationInvalidationStatsQueryServiceImpl statsQueryService;

	@Test
	@DisplayName("统计摘要：应汇总 Outbox 失败率与幂等事件计数")
	void getSummary_shouldAggregateOutboxAndEventStats() {
		AuthorizationInvalidationOutboxStatsPO outboxStats = new AuthorizationInvalidationOutboxStatsPO();
		outboxStats.setTotalCount(10L);
		outboxStats.setPendingCount(1L);
		outboxStats.setProcessingCount(2L);
		outboxStats.setSuccessCount(5L);
		outboxStats.setFailedCount(1L);
		outboxStats.setDeadCount(1L);
		when(authorizationInvalidationOutboxOpsService.getOutboxStats()).thenReturn(outboxStats);

		AuthorizationInvalidationEventStatsPO eventStats = new AuthorizationInvalidationEventStatsPO();
		eventStats.setTotalCount(8L);
		eventStats.setProcessingCount(1L);
		eventStats.setCompletedCount(7L);
		when(authorizationInvalidationEventOpsService.getEventStats()).thenReturn(eventStats);

		AuthorizationInvalidationSummaryVO summary = statsQueryService.getSummary();

		assertEquals(10L, summary.getOutbox().getTotalCount());
		assertEquals(1L, summary.getOutbox().getDeadCount());
		assertEquals(new BigDecimal("20.00"), summary.getOutbox().getFailureRatePercent());
		assertEquals(8L, summary.getEvent().getTotalCount());
		assertEquals(1L, summary.getEvent().getProcessingCount());
		verify(authorizationInvalidationOutboxOpsService).getOutboxStats();
		verify(authorizationInvalidationEventOpsService).getEventStats();
	}

	@Test
	@DisplayName("统计摘要：事件统计不可用时抛出 SERVICE_UNAVAILABLE")
	void getSummary_shouldThrowWhenEventSummaryUnavailable() {
		when(authorizationInvalidationOutboxOpsService.getOutboxStats())
			.thenReturn(new AuthorizationInvalidationOutboxStatsPO());
		when(authorizationInvalidationEventOpsService.getEventStats())
			.thenThrow(new SystemBusinessException(SERVICE_UNAVAILABLE));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class, statsQueryService::getSummary);

		assertEquals(SERVICE_UNAVAILABLE, exception.getResultCode());
	}

	@Test
	@DisplayName("失败率趋势：应查询分桶并计算失败率")
	void getFailureRateTrend_shouldQueryBucketsAndCalculateFailureRate() {
		AuthorizationInvalidationFailureRateTrendQuery query = new AuthorizationInvalidationFailureRateTrendQuery();
		query.setGranularity(AuthorizationInvalidationFailureRateTrendGranularity.DAY);
		query.setStartTime(LocalDateTime.of(2026, 6, 1, 0, 0).toInstant(java.time.ZoneOffset.UTC));
		query.setEndTime(LocalDateTime.of(2026, 6, 7, 23, 59, 59).toInstant(java.time.ZoneOffset.UTC));

		AuthorizationInvalidationFailureRateTrendBucketPO bucketPo = new AuthorizationInvalidationFailureRateTrendBucketPO();
		bucketPo.setBucket("2026-06-01");
		bucketPo.setTotalCount(10L);
		bucketPo.setFailedCount(1L);
		bucketPo.setDeadCount(1L);
		when(authorizationInvalidationOutboxOpsMapper
			.selectFailureRateTrendBuckets(any(AuthorizationInvalidationFailureRateTrendQuery.class)))
			.thenReturn(List.of(bucketPo));

		AuthorizationInvalidationFailureRateTrendVO trend = statsQueryService.getFailureRateTrend(query);

		assertEquals("DAY", trend.getGranularity());
		assertEquals(query.getStartTime(), trend.getStartTime());
		assertEquals(query.getEndTime(), trend.getEndTime());
		assertEquals(1, trend.getPoints().size());
		assertEquals(new BigDecimal("20.00"), trend.getPoints().get(0).getFailureRatePercent());
	}

	@Test
	@DisplayName("失败率趋势：未指定起止时间时应使用 days 默认值")
	void getFailureRateTrend_shouldDefaultDaysWhenTimeRangeMissing() {
		AuthorizationInvalidationFailureRateTrendQuery query = new AuthorizationInvalidationFailureRateTrendQuery();
		query.setGranularity(AuthorizationInvalidationFailureRateTrendGranularity.WEEK);
		query.setDays(7);
		when(authorizationInvalidationOutboxOpsMapper
			.selectFailureRateTrendBuckets(any(AuthorizationInvalidationFailureRateTrendQuery.class)))
			.thenReturn(List.of());

		AuthorizationInvalidationFailureRateTrendVO trend = statsQueryService.getFailureRateTrend(query);

		assertEquals("WEEK", trend.getGranularity());
		assertNotNull(trend.getStartTime());
		assertNotNull(trend.getEndTime());
		assertNotNull(query.getStartTime());
		assertNotNull(query.getEndTime());
	}

}
