package com.auth.service.system.authorization.service.impl;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.authorization.integration.feign.AuthInnerFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserSessionRevocationServiceImpl} 单元测试
 */
@DisplayName("UserSessionRevocationServiceImpl 会话撤销")
@ExtendWith(MockitoExtension.class)
class UserSessionRevocationServiceImplTest {

	@Mock
	private AuthInnerFeignClient authInnerFeignClient;

	@InjectMocks
	private UserSessionRevocationServiceImpl userSessionRevocationService;

	@Test
	@DisplayName("空列表：不调用 Feign")
	void revokeAllSessionsIgnoresEmptyIds() {
		userSessionRevocationService.revokeAllSessions(List.of());

		verify(authInnerFeignClient, never()).kickAllSessions(anyList());
	}

	@Test
	@DisplayName("有用户 ID：调用内部踢会话接口")
	void revokeAllSessionsShouldCallFeign() {
		List<Long> userIds = List.of(2L, 3L);
		when(authInnerFeignClient.kickAllSessions(userIds)).thenReturn(Result.success());

		userSessionRevocationService.revokeAllSessions(userIds);

		verify(authInnerFeignClient).kickAllSessions(userIds);
	}

	@Test
	@DisplayName("Feign 抛异常：吞掉异常不向外传播")
	void revokeAllSessionsShouldSwallowFeignException() {
		List<Long> userIds = List.of(2L);
		when(authInnerFeignClient.kickAllSessions(userIds)).thenThrow(new RuntimeException("boom"));

		userSessionRevocationService.revokeAllSessions(userIds);

		verify(authInnerFeignClient).kickAllSessions(userIds);
	}

}
