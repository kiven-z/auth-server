package com.auth.service.system.authorization.outbox;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.admin.model.form.authorizationinvalidation.AuthorizationInvalidationOutboxRetryForm;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationOutboxRetryResultVO;
import com.auth.service.system.authorization.config.AuthorizationInvalidationProperties;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationOutboxProcessor;
import com.auth.service.system.authorization.outbox.mapper.SysAuthorizationInvalidationOutboxMapper;
import com.auth.service.system.authorization.outbox.model.SysAuthorizationInvalidationOutboxEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.feign.AuthInnerFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static com.auth.service.system.admin.exception.SystemAdminResultCode.AUTH_INVALIDATION_OUTBOX_PROCESSING_LOCKED;
import static com.auth.service.system.admin.exception.SystemAdminResultCode.AUTH_INVALIDATION_OUTBOX_RETRY_NOT_ALLOWED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationOutboxManualRetryService} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationInvalidationOutboxManualRetryService Outbox 人工重试")
class AuthorizationInvalidationOutboxManualRetryServiceTest {

	@Mock
	private SysAuthorizationInvalidationOutboxMapper outboxMapper;

	@Mock
	private AuthorizationInvalidationOutboxProcessor outboxProcessor;

	@Mock
	private AuthInnerFeignClient authInnerFeignClient;

	private final AuthorizationInvalidationProperties properties = new AuthorizationInvalidationProperties();

	private AuthorizationInvalidationOutboxManualRetryService manualRetryService;

	@BeforeEach
	void setUpService() {
		manualRetryService = new AuthorizationInvalidationOutboxManualRetryService(outboxMapper, outboxProcessor,
				authInnerFeignClient, properties);
	}

	private static SysAuthorizationInvalidationOutboxEntity buildRow(Long id, String status) {
		SysAuthorizationInvalidationOutboxEntity row = new SysAuthorizationInvalidationOutboxEntity();
		row.setId(id);
		row.setEventId("role:1001");
		row.setStatus(status);
		row.setVersion(1L);
		return row;
	}

	@Test
	@DisplayName("SUCCESS 状态不允许人工重试")
	void retryById_shouldRejectSuccessStatus() {
		when(outboxMapper.selectById(1L))
			.thenReturn(buildRow(1L, AuthorizationInvalidationOutboxStatus.SUCCESS.name()));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> manualRetryService.retryById(1L, null));

		assertEquals(AUTH_INVALIDATION_OUTBOX_RETRY_NOT_ALLOWED, exception.getResultCode());
		verify(outboxProcessor, never()).processById(any());
	}

	@Test
	@DisplayName("DEAD 状态应先 reset 再同步投递")
	void retryById_shouldResetDeadAndDispatch() {
		SysAuthorizationInvalidationOutboxEntity deadRow = buildRow(2L,
				AuthorizationInvalidationOutboxStatus.DEAD.name());
		SysAuthorizationInvalidationOutboxEntity pendingRow = buildRow(2L,
				AuthorizationInvalidationOutboxStatus.PENDING.name());
		when(outboxMapper.selectById(2L)).thenReturn(deadRow, pendingRow);
		when(outboxMapper.resetForManualRetry(2L, 1L)).thenReturn(1);
		when(authInnerFeignClient.releaseInvalidationEventClaim("role:1001")).thenReturn(Result.success(false));
		when(outboxProcessor.processById(2L)).thenReturn(true);

		AuthorizationInvalidationOutboxRetryResultVO result = manualRetryService.retryById(2L, null);

		assertEquals("DEAD", result.getPreviousStatus());
		assertEquals(AuthorizationInvalidationOutboxStatus.PENDING.name(), result.getCurrentStatus());
		assertTrue(result.getDispatched());
		verify(outboxMapper).resetForManualRetry(2L, 1L);
	}

	@Test
	@DisplayName("PROCESSING 未超时且未 force 时应拒绝")
	void retryById_shouldRejectFreshProcessingWithoutForce() {
		SysAuthorizationInvalidationOutboxEntity processingRow = buildRow(3L,
				AuthorizationInvalidationOutboxStatus.PROCESSING.name());
		processingRow.setLockedAt(LocalDateTime.now().plusMinutes(1));
		when(outboxMapper.selectById(3L)).thenReturn(processingRow);

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> manualRetryService.retryById(3L, null));

		assertEquals(AUTH_INVALIDATION_OUTBOX_PROCESSING_LOCKED, exception.getResultCode());
	}

	@Test
	@DisplayName("PROCESSING force=true 时应 reset 并投递")
	void retryById_shouldForceProcessingRetry() {
		SysAuthorizationInvalidationOutboxEntity processingRow = buildRow(4L,
				AuthorizationInvalidationOutboxStatus.PROCESSING.name());
		processingRow.setLockedAt(LocalDateTime.now());
		SysAuthorizationInvalidationOutboxEntity pendingRow = buildRow(4L,
				AuthorizationInvalidationOutboxStatus.PENDING.name());
		when(outboxMapper.selectById(4L)).thenReturn(processingRow, pendingRow);
		when(outboxMapper.resetForManualRetry(4L, 1L)).thenReturn(1);
		when(authInnerFeignClient.releaseInvalidationEventClaim("role:1001")).thenReturn(Result.success(true));
		when(outboxProcessor.processById(4L)).thenReturn(false);

		AuthorizationInvalidationOutboxRetryForm form = new AuthorizationInvalidationOutboxRetryForm();
		form.setForce(true);
		AuthorizationInvalidationOutboxRetryResultVO result = manualRetryService.retryById(4L, form);

		assertEquals("PROCESSING", result.getPreviousStatus());
		assertTrue(result.getClaimReleased());
		assertFalse(result.getDispatched());
		verify(outboxMapper).resetForManualRetry(4L, 1L);
	}

}
