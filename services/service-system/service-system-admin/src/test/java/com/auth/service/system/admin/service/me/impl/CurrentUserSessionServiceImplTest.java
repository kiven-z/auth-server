package com.auth.service.system.admin.service.me.impl;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.security.SecurityRequestAttributes;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.query.UserSessionQueryOperations;
import com.auth.service.system.authorization.dispatch.query.UserSessionSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link CurrentUserSessionServiceImpl} 单元测试
 */
@DisplayName("CurrentUserSessionServiceImpl 个人中心会话")
@ExtendWith(MockitoExtension.class)
class CurrentUserSessionServiceImplTest {

	@Mock
	private UserSessionQueryOperations userSessionQueryOperations;

	@Mock
	private UserReferenceChecker userReferenceChecker;

	@InjectMocks
	private CurrentUserSessionServiceImpl currentUserSessionService;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	@DisplayName("查询会话：标记当前设备")
	void listMySessionsMarksCurrentSession() {
		setCurrentUser();

		UserSessionSnapshot current = UserSessionSnapshot.builder().sessionId("current-jti").build();
		UserSessionSnapshot other = UserSessionSnapshot.builder().sessionId("other-jti").build();
		when(userSessionQueryOperations.listActiveSessions(100L)).thenReturn(List.of(current, other));

		var sessions = currentUserSessionService.listMySessions();

		assertThat(sessions).hasSize(2);
		assertThat(sessions.get(0).getCurrent()).isTrue();
		assertThat(sessions.get(1).getCurrent()).isFalse();
	}

	@Test
	@DisplayName("踢出会话：委托 UserSessionQueryOperations")
	void kickSessionDelegatesToQueryOperations() {
		setCurrentUser();

		currentUserSessionService.kickSession("jti-1");

		verify(userSessionQueryOperations).kickSession(100L, "jti-1");
	}

	private void setCurrentUser() {
		AuthProfile profile = AuthProfile.builder().userId(100L).username("tester").build();
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(profile, null));
		when(userReferenceChecker.getExistingActive(100L)).thenReturn(new UserEntity());

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute(SecurityRequestAttributes.SESSION_ID, "current-jti");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

}
