package com.auth.service.auth.authorization.job;

import com.auth.service.auth.authorization.application.ops.AuthorizationInvalidationEventOpsApplicationService;
import com.auth.service.auth.authorization.config.AuthorizationInvalidationProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * {@link AuthorizationInvalidationProcessingClaimCleanupJob} 单元测试
 */
@DisplayName("AuthorizationInvalidationProcessingClaimCleanupJob processing 占位清理")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationProcessingClaimCleanupJobTest {

	@Mock
	private AuthorizationInvalidationEventOpsApplicationService authorizationInvalidationEventOpsApplicationService;

	@Mock
	private AuthorizationInvalidationProperties properties;

	@InjectMocks
	private AuthorizationInvalidationProcessingClaimCleanupJob cleanupJob;

	@Test
	@DisplayName("未启用时不执行清理")
	void cleanupStaleProcessingClaims_shouldSkipWhenDisabled() {
		when(properties.getProcessingClaimCleanupEnabled()).thenReturn(false);

		cleanupJob.cleanupStaleProcessingClaims();

		verify(authorizationInvalidationEventOpsApplicationService, never()).cleanupStaleProcessingClaims(any(),
				anyInt());
	}

	@Test
	@DisplayName("启用时按配置调用批量释放")
	void cleanupStaleProcessingClaims_shouldInvokeOpsServiceWhenEnabled() {
		when(properties.getProcessingClaimCleanupEnabled()).thenReturn(true);
		when(properties.getProcessingClaimTimeoutMinutes()).thenReturn(30);
		when(properties.getProcessingClaimCleanupBatchSize()).thenReturn(100);
		when(authorizationInvalidationEventOpsApplicationService.cleanupStaleProcessingClaims(any(), anyInt()))
			.thenReturn(2);

		cleanupJob.cleanupStaleProcessingClaims();

		verify(authorizationInvalidationEventOpsApplicationService).cleanupStaleProcessingClaims(any(), anyInt());
	}

}
