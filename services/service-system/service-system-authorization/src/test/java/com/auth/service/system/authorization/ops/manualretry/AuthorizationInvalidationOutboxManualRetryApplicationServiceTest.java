package com.auth.service.system.authorization.ops.manualretry;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationOutboxProcessor;
import com.auth.service.system.authorization.exception.AuthorizationInvalidationOpsResultCode;
import com.auth.service.system.authorization.feign.AuthInnerFeignClient;
import com.auth.service.system.authorization.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.model.entity.SysAuthorizationInvalidationOutboxEntity;
import com.auth.service.system.authorization.model.enums.AuthorizationInvalidationOutboxStatus;
import com.auth.service.system.authorization.ops.AuthorizationInvalidationOutboxManualRetryApplicationService;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link AuthorizationInvalidationOutboxManualRetryApplicationService} 单元测试
 */
@DisplayName("AuthorizationInvalidationOutboxManualRetryApplicationService Outbox 人工重试")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationOutboxManualRetryApplicationServiceTest {

	private final AuthorizationInvalidationProperties properties = new AuthorizationInvalidationProperties();

	@Mock
	private SysAuthorizationInvalidationOutboxMapper outboxMapper;

	@Mock
	private AuthorizationInvalidationOutboxProcessor outboxProcessor;

	@Mock
	private AuthInnerFeignClient authInnerFeignClient;

	private AuthorizationInvalidationOutboxManualRetryApplicationService manualRetryService;

	private static SysAuthorizationInvalidationOutboxEntity buildRow(Long id, String status) {
		SysAuthorizationInvalidationOutboxEntity row = new SysAuthorizationInvalidationOutboxEntity();
		row.setId(id);
		row.setEventId("role:1001");
		row.setStatus(status);
		row.setVersion(1L);
		return row;
	}

	@BeforeEach
	void setUpService() {
		manualRetryService = new AuthorizationInvalidationOutboxManualRetryApplicationService(outboxMapper,
				outboxProcessor, authInnerFeignClient, properties);
	}

	@Test
	@DisplayName("SUCCESS 状态不允许人工重试")
	void retryManual_shouldRejectSuccessStatus() {
		// 已 SUCCESS 的 Outbox 不允许再次人工重试
		when(outboxMapper.selectById(1L))
			.thenReturn(buildRow(1L, AuthorizationInvalidationOutboxStatus.SUCCESS.name()));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> manualRetryService.retryManual(1L, false));

		assertEquals(AuthorizationInvalidationOpsResultCode.RETRY_NOT_ALLOWED, exception.getResultCode());
		verify(outboxProcessor, never()).processById(any());
	}

	@Test
	@DisplayName("DEAD 状态应先 reset 再同步投递")
	void retryManual_shouldResetDeadAndDispatch() {
		// DEAD 需先 reset 再触发一次同步投递
		SysAuthorizationInvalidationOutboxEntity deadRow = buildRow(2L,
				AuthorizationInvalidationOutboxStatus.DEAD.name());
		SysAuthorizationInvalidationOutboxEntity pendingRow = buildRow(2L,
				AuthorizationInvalidationOutboxStatus.PENDING.name());
		when(outboxMapper.selectById(2L)).thenReturn(deadRow, pendingRow);
		when(outboxMapper.resetForManualRetry(2L, 1L)).thenReturn(1);
		when(authInnerFeignClient.releaseInvalidationEventClaim("role:1001")).thenReturn(Result.success(false));
		when(outboxProcessor.processById(2L)).thenReturn(true);

		var outcome = manualRetryService.retryManual(2L, false);

		assertEquals("DEAD", outcome.previousStatus());
		assertEquals(AuthorizationInvalidationOutboxStatus.PENDING.name(), outcome.currentStatus());
		assertTrue(outcome.dispatched());
		verify(outboxMapper).resetForManualRetry(2L, 1L);
	}

	@Test
	@DisplayName("PROCESSING 未超时且未 force 时应拒绝")
	void retryManual_shouldRejectFreshProcessingWithoutForce() {
		// PROCESSING 占位未超时且未 force 时不允许重试
		SysAuthorizationInvalidationOutboxEntity processingRow = buildRow(3L,
				AuthorizationInvalidationOutboxStatus.PROCESSING.name());
		processingRow.setLockedAt(LocalDateTime.now().plusMinutes(1));
		when(outboxMapper.selectById(3L)).thenReturn(processingRow);

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> manualRetryService.retryManual(3L, false));

		assertEquals(AuthorizationInvalidationOpsResultCode.PROCESSING_LOCKED, exception.getResultCode());
	}

	@Test
	@DisplayName("PROCESSING force=true 时应 reset 并投递")
	void retryManual_shouldForceProcessingRetry() {
		// force=true 时允许重置 PROCESSING 并再次投递
		SysAuthorizationInvalidationOutboxEntity processingRow = buildRow(4L,
				AuthorizationInvalidationOutboxStatus.PROCESSING.name());
		processingRow.setLockedAt(LocalDateTime.now());
		SysAuthorizationInvalidationOutboxEntity pendingRow = buildRow(4L,
				AuthorizationInvalidationOutboxStatus.PENDING.name());
		when(outboxMapper.selectById(4L)).thenReturn(processingRow, pendingRow);
		when(outboxMapper.resetForManualRetry(4L, 1L)).thenReturn(1);
		when(authInnerFeignClient.releaseInvalidationEventClaim("role:1001")).thenReturn(Result.success(true));
		when(outboxProcessor.processById(4L)).thenReturn(false);

		var outcome = manualRetryService.retryManual(4L, true);

		assertEquals("PROCESSING", outcome.previousStatus());
		assertTrue(outcome.claimReleased());
		assertFalse(outcome.dispatched());
		verify(outboxMapper).resetForManualRetry(4L, 1L);
	}

}
