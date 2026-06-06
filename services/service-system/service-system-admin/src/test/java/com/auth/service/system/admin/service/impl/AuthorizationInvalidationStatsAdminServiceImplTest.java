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

import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationSummaryVO;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxStatsPO;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationOutboxOpsService;
import com.auth.service.system.common.exception.SystemBusinessException;

/**
 * {@link AuthorizationInvalidationStatsAdminServiceImpl} 单元测试
 */
@DisplayName("AuthorizationInvalidationStatsAdminServiceImpl 运维统计")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationStatsAdminServiceImplTest {

	@Mock
	private AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	@Mock
	private AuthorizationInvalidationEventOpsService authorizationInvalidationEventOpsService;

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
		when(authorizationInvalidationOutboxOpsService.getOutboxStats()).thenReturn(outboxStats);

		AuthorizationInvalidationEventStatsPO eventStats = new AuthorizationInvalidationEventStatsPO();
		eventStats.setTotalCount(8L);
		eventStats.setProcessingCount(1L);
		eventStats.setCompletedCount(7L);
		when(authorizationInvalidationEventOpsService.getEventStats()).thenReturn(eventStats);

		AuthorizationInvalidationSummaryVO summary = statsAdminService.getSummary();

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

		SystemBusinessException exception = assertThrows(SystemBusinessException.class, statsAdminService::getSummary);

		assertEquals(SERVICE_UNAVAILABLE, exception.getResultCode());
	}

}
