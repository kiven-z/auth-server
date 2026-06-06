package com.auth.service.system.admin.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.application.query.AuthorizationInvalidationOutboxQueryApplicationService;
import com.auth.service.system.authorization.outbox.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.outbox.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.outbox.model.vo.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationOutboxAdminServiceImpl} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationInvalidationOutboxAdminServiceImpl Outbox 管理端查询")
class AuthorizationInvalidationOutboxAdminServiceImplTest {

	@Mock
	private AuthorizationInvalidationOutboxQueryApplicationService queryApplicationService;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@InjectMocks
	private AuthorizationInvalidationOutboxAdminServiceImpl adminService;

	@Test
	@DisplayName("分页查询：应 enrich 审计用户名")
	void getPage_shouldEnrichAuditUsernames() {
		AuthorizationInvalidationOutboxPageVO pageVO = new AuthorizationInvalidationOutboxPageVO();
		pageVO.setId(1L);
		pageVO.setEventId("evt-1");
		pageVO.setStatus("PENDING");

		PageResponse<AuthorizationInvalidationOutboxPageVO> pageData = PageResponse.of(1L, 10L, 1L, List.of(pageVO));
		when(queryApplicationService.getPage(org.mockito.ArgumentMatchers.any())).thenReturn(pageData);

		PageResponse<AuthorizationInvalidationOutboxPageVO> response = adminService
			.getPage(new AuthorizationInvalidationOutboxQuery());

		assertEquals(1L, response.getTotal());
		assertEquals("evt-1", response.getList().get(0).getEventId());
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("详情查询：应用层抛出 DATA_NOT_EXIST 时应向上传递")
	void getDetail_shouldPropagateNotFound() {
		when(queryApplicationService.getDetail(1L)).thenThrow(new SystemBusinessException(DATA_NOT_EXIST));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> adminService.getDetail(1L));

		assertEquals(DATA_NOT_EXIST, exception.getResultCode());
	}

	@Test
	@DisplayName("详情查询：成功时应 enrich 审计用户名")
	void getDetail_shouldEnrichAuditUsernames() {
		AuthorizationInvalidationOutboxDetailVO detailVO = new AuthorizationInvalidationOutboxDetailVO();
		detailVO.setId(2L);
		detailVO.setEventId("evt-detail");
		when(queryApplicationService.getDetail(2L)).thenReturn(detailVO);

		AuthorizationInvalidationOutboxDetailVO result = adminService.getDetail(2L);

		assertEquals("evt-detail", result.getEventId());
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

}
