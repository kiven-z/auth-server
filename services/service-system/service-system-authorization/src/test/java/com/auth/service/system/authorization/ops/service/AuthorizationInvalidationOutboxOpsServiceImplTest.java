package com.auth.service.system.authorization.ops.service;

import com.auth.service.system.authorization.ops.manualretry.AuthorizationInvalidationOutboxManualRetryApplicationService;
import com.auth.service.system.authorization.ops.mapper.AuthorizationInvalidationOutboxOpsMapper;
import com.auth.service.system.authorization.ops.model.AuthorizationInvalidationOutboxManualRetryOutcome;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;

/**
 * {@link AuthorizationInvalidationOutboxOpsServiceImpl} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationInvalidationOutboxOpsServiceImpl 运维门面")
class AuthorizationInvalidationOutboxOpsServiceImplTest {

	@Mock
	private AuthorizationInvalidationOutboxOpsMapper authorizationInvalidationOutboxOpsMapper;

	@Mock
	private AuthorizationInvalidationOutboxManualRetryApplicationService manualRetryApplicationService;

	private AuthorizationInvalidationOutboxOpsServiceImpl opsService;

	@BeforeEach
	void setUpService() {
		opsService = new AuthorizationInvalidationOutboxOpsServiceImpl(authorizationInvalidationOutboxOpsMapper,
				manualRetryApplicationService);
	}

	@Test
	@DisplayName("retryManual 应委托给人工重试应用服务")
	void retryManual_shouldDelegateToManualRetryApplicationService() {
		// 运维门面只负责转发人工重试请求
		AuthorizationInvalidationOutboxManualRetryOutcome expected = new AuthorizationInvalidationOutboxManualRetryOutcome(
				1L, "role:1001", "DEAD", "PENDING", null, true, false);
		when(manualRetryApplicationService.retryManual(1L, true)).thenReturn(expected);

		AuthorizationInvalidationOutboxManualRetryOutcome outcome = opsService.retryManual(1L, true);

		assertSame(expected, outcome);
		verify(manualRetryApplicationService).retryManual(1L, true);
	}

	@Test
	@DisplayName("getDetail 记录不存在时抛出 DATA_NOT_EXIST")
	void getDetail_shouldThrowWhenMissing() {
		when(authorizationInvalidationOutboxOpsMapper.selectDetailById(99L)).thenReturn(null);

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> opsService.getDetail(99L));

		assertSame(DATA_NOT_EXIST, exception.getResultCode());
	}

}
