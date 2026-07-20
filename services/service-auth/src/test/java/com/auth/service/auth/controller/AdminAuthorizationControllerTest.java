package com.auth.service.auth.controller;

import com.auth.service.auth.service.AdminAuthorizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * {@link AdminAuthorizationController} 单元测试
 */
@DisplayName("AdminAuthorizationController 管理端授权")
@ExtendWith(MockitoExtension.class)
class AdminAuthorizationControllerTest {

	@Mock
	private AdminAuthorizationService adminAuthorizationService;

	@InjectMocks
	private AdminAuthorizationController adminAuthorizationController;

	@Test
	@DisplayName("批量刷新授权画像：委托 AdminAuthorizationService")
	void refreshAuthorizationShouldDelegateToAdminService() {
		List<Long> userIds = List.of(2L, 3L);

		adminAuthorizationController.refreshAuthorization(userIds);

		verify(adminAuthorizationService).refreshUserCache(userIds);
	}

}
