package com.auth.service.system.common.controller;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.common.service.AuditUserDisplayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link InternalAuditUserDisplayController} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("InternalAuditUserDisplayController 内部审计用户名")
@ExtendWith(MockitoExtension.class)
class InternalAuditUserDisplayControllerTest {

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private InternalAuditUserDisplayController controller;

	@BeforeEach
	void setUp() {
		controller = new InternalAuditUserDisplayController(auditUserDisplayService);
	}

	@Test
	@DisplayName("POST /usernames：委托服务并包装 Result.success")
	void mapUsernamesDelegatesAndWrapsResult() {
		when(auditUserDisplayService.mapUsernamesByIds(List.of(1L, 2L))).thenReturn(Map.of(1L, "a", 2L, "b"));

		Result<Map<Long, String>> result = controller.mapUsernames(List.of(1L, 2L));

		assertThat(result.getData()).containsEntry(1L, "a").containsEntry(2L, "b");
		verify(auditUserDisplayService).mapUsernamesByIds(List.of(1L, 2L));
	}

}
