package com.auth.service.system.authorization.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.model.vo.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.authorization.service.AuthorizationInvalidationOutboxOpsService;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationOutboxQueryServiceImpl} 单元测试
 */
@DisplayName("AuthorizationInvalidationOutboxQueryServiceImpl Outbox 运维查询")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationOutboxQueryServiceImplTest {

	@Mock
	private AuthorizationInvalidationOutboxOpsService authorizationInvalidationOutboxOpsService;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@InjectMocks
	private AuthorizationInvalidationOutboxQueryServiceImpl queryService;

	private static AuthorizationInvalidationOutboxPageRowPO buildPageRow() {
		AuthorizationInvalidationOutboxPageRowPO row = new AuthorizationInvalidationOutboxPageRowPO();
		row.setId(1L);
		row.setEventId("evt-1");
		row.setChangeKind("ROLE");
		row.setStatus("PENDING");
		row.setRetryCount(0);
		row.setMaxRetry(5);
		row.setCreateTime(LocalDateTime.now());
		return row;
	}

	@Test
	@DisplayName("分页查询：应返回 Outbox 分页结果并 enrich 审计用户名")
	void getPage_shouldReturnPageResponseAndEnrichAuditUsernames() {
		AuthorizationInvalidationOutboxQuery query = new AuthorizationInvalidationOutboxQuery();
		query.setPageIndex(1);
		query.setPageSize(10);

		AuthorizationInvalidationOutboxPageRowPO row = buildPageRow();
		Page<AuthorizationInvalidationOutboxPageRowPO> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(row));
		when(authorizationInvalidationOutboxOpsService.getPage(any())).thenReturn(page);

		PageResponse<AuthorizationInvalidationOutboxPageVO> response = queryService.getPage(query);

		assertEquals(1L, response.getTotal());
		assertEquals("evt-1", response.getList().get(0).getEventId());
		assertEquals("PENDING", response.getList().get(0).getStatus());
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());
	}

	@Test
	@DisplayName("详情查询：记录不存在时抛出 DATA_NOT_EXIST")
	void getDetail_shouldThrowWhenMissing() {
		when(authorizationInvalidationOutboxOpsService.getDetail(99L))
			.thenThrow(new SystemBusinessException(DATA_NOT_EXIST));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> queryService.getDetail(99L));

		assertEquals(DATA_NOT_EXIST, exception.getResultCode());
	}

	@Test
	@DisplayName("详情查询：存在记录时返回详情 VO 并 enrich 审计用户名")
	void getDetail_shouldReturnDetailVoAndEnrichAuditUsernames() {
		AuthorizationInvalidationOutboxDetailRowPO detailRow = new AuthorizationInvalidationOutboxDetailRowPO();
		detailRow.setId(10L);
		detailRow.setEventId("evt-detail");
		detailRow.setChangeKind("ROLE");
		detailRow.setStatus("DEAD");
		detailRow.setPayload("{\"roleCode\":\"ADMIN\"}");
		detailRow.setLastError("auth invalidate failed");
		when(authorizationInvalidationOutboxOpsService.getDetail(10L)).thenReturn(detailRow);

		AuthorizationInvalidationOutboxDetailVO detailVO = queryService.getDetail(10L);

		assertEquals("evt-detail", detailVO.getEventId());
		assertEquals("DEAD", detailVO.getStatus());
		assertEquals("{\"roleCode\":\"ADMIN\"}", detailVO.getPayload());
		verify(auditUserDisplayService).enrichAuditUsernames(anyList(), isNull(), isNull());
	}

}
