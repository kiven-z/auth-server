package com.auth.service.system.admin.service.impl;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.mapper.AuthorizationInvalidationOutboxAdminMapper;
import com.auth.service.system.admin.model.po.authorizationinvalidation.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationSummaryVO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.feign.AuthInnerFeignClient;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventStatsInnerDTO;

/**
 * {@link AuthorizationInvalidationStatsAdminServiceImpl} 单元测试
 */
@DisplayName("AuthorizationInvalidationStatsAdminServiceImpl 运维统计")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationStatsAdminServiceImplTest {

	@Mock
	private AuthorizationInvalidationOutboxAdminMapper authorizationInvalidationOutboxAdminMapper;

	@Mock
	private AuthInnerFeignClient authInnerFeignClient;

	@InjectMocks
	private AuthorizationInvalidationStatsAdminServiceImpl statsAdminService;

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
		when(authorizationInvalidationOutboxAdminMapper.selectOutboxStats()).thenReturn(outboxStats);

		AuthorizationInvalidationEventStatsInnerDTO eventStats = new AuthorizationInvalidationEventStatsInnerDTO();
		eventStats.setTotalCount(8L);
		eventStats.setProcessingCount(1L);
		eventStats.setCompletedCount(7L);
		when(authInnerFeignClient.getInvalidationEventSummary()).thenReturn(Result.success(eventStats));

		AuthorizationInvalidationSummaryVO summary = statsAdminService.getSummary();

		assertEquals(10L, summary.getOutbox().getTotalCount());
		assertEquals(1L, summary.getOutbox().getDeadCount());
		assertEquals(new BigDecimal("20.00"), summary.getOutbox().getFailureRatePercent());
		assertEquals(8L, summary.getEvent().getTotalCount());
		assertEquals(1L, summary.getEvent().getProcessingCount());
		verify(authorizationInvalidationOutboxAdminMapper).selectOutboxStats();
		verify(authInnerFeignClient).getInvalidationEventSummary();
	}

	@Test
	@DisplayName("统计摘要：auth 统计不可用时抛出 SERVICE_UNAVAILABLE")
	void getSummary_shouldThrowWhenEventSummaryUnavailable() {
		when(authorizationInvalidationOutboxAdminMapper.selectOutboxStats())
			.thenReturn(new AuthorizationInvalidationOutboxStatsPO());
		when(authInnerFeignClient.getInvalidationEventSummary()).thenReturn(Result.error("unavailable"));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class, statsAdminService::getSummary);

		assertEquals(SERVICE_UNAVAILABLE, exception.getResultCode());
	}

}
