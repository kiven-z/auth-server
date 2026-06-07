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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationEventHistoryCleanupJob} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationInvalidationEventHistoryCleanupJob SUCCESS 历史清理")
class AuthorizationInvalidationEventHistoryCleanupJobTest {

	@Mock
	private AuthorizationInvalidationEventOpsApplicationService authorizationInvalidationEventOpsApplicationService;

	@Mock
	private AuthorizationInvalidationProperties properties;

	@InjectMocks
	private AuthorizationInvalidationEventHistoryCleanupJob historyCleanupJob;

	@Test
	@DisplayName("未启用时不执行清理")
	void purgeCompletedHistory_shouldSkipWhenDisabled() {
		when(properties.getSuccessCleanupEnabled()).thenReturn(false);

		historyCleanupJob.purgeCompletedHistory();

		verify(authorizationInvalidationEventOpsApplicationService, never()).purgeCompletedBefore(any(), anyInt());
	}

	@Test
	@DisplayName("启用时循环批量删除直到不足一批")
	void purgeCompletedHistory_shouldPurgeInBatchesWhenEnabled() {
		when(properties.getSuccessCleanupEnabled()).thenReturn(true);
		when(properties.getSuccessRetentionDays()).thenReturn(90);
		when(properties.getSuccessCleanupBatchSize()).thenReturn(500);
		when(authorizationInvalidationEventOpsApplicationService.purgeCompletedBefore(any(), anyInt()))
			.thenReturn(500, 120);

		historyCleanupJob.purgeCompletedHistory();

		verify(authorizationInvalidationEventOpsApplicationService, times(2)).purgeCompletedBefore(any(), anyInt());
	}

}
