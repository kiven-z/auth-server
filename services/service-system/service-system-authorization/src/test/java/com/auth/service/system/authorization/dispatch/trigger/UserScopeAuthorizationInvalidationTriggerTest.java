package com.auth.service.system.authorization.dispatch.trigger;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.AuthorizationInvalidateRequest;
import com.auth.module.security.contract.dto.invalidation.UserScopeInvalidatePayload;
import com.auth.service.system.authorization.dispatch.AuthorizationInvalidationCoordinator;
import com.auth.service.system.common.model.SystemAuditBizModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link UserScopeAuthorizationInvalidationTrigger} 单元测试
 */
@DisplayName("UserScopeAuthorizationInvalidationTrigger 用户授权失效触发")
@ExtendWith(MockitoExtension.class)
class UserScopeAuthorizationInvalidationTriggerTest {

	@Mock
	private AuthorizationInvalidationCoordinator invalidationCoordinator;

	@InjectMocks
	private UserScopeAuthorizationInvalidationTrigger userScopeInvalidationTrigger;

	@Test
	@DisplayName("空列表或全 null 时不提交失效事件")
	void submitByUserIds_whenEmpty_shouldSkip() {
		userScopeInvalidationTrigger.submitByUserIds(Collections.emptyList(), "update");
		userScopeInvalidationTrigger.submitByUserIds(Arrays.asList(null, null), "delete");

		verify(invalidationCoordinator, never()).submit(org.mockito.ArgumentMatchers.any(),
				eq(SystemAuditBizModule.SYS_USER), org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("按 userId 提交 USER_SCOPE 失效事件")
	void submitByUserIds_shouldSubmitUserScopeInvalidation() {
		userScopeInvalidationTrigger.submitByUserIds(Arrays.asList(10L, null, 10L, 20L), "update");

		ArgumentCaptor<AuthorizationInvalidateRequest> requestCaptor = ArgumentCaptor
			.forClass(AuthorizationInvalidateRequest.class);
		verify(invalidationCoordinator).submit(requestCaptor.capture(), eq(SystemAuditBizModule.SYS_USER),
				eq("update:10,20"));

		AuthorizationInvalidateRequest request = requestCaptor.getValue();
		assertTrue(request.eventId().startsWith("user:"));
		assertEquals(AuthorizationChangeKind.USER_SCOPE, request.kind());
		assertInstanceOf(UserScopeInvalidatePayload.class, request.payload());
		assertEquals(List.of(10L, 20L), ((UserScopeInvalidatePayload) request.payload()).userIds());
	}

}
