package com.auth.service.system.authorization.application.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.outbox.model.po.AuthorizationInvalidationOutboxDetailRowPO;
import com.auth.service.system.authorization.outbox.model.po.AuthorizationInvalidationOutboxPageRowPO;
import com.auth.service.system.authorization.outbox.model.query.AuthorizationInvalidationOutboxQuery;
import com.auth.service.system.authorization.outbox.model.vo.AuthorizationInvalidationOutboxDetailVO;
import com.auth.service.system.authorization.outbox.model.vo.AuthorizationInvalidationOutboxPageVO;
import com.auth.service.system.authorization.outbox.port.InvalidationOutboxQueryPort;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationOutboxQueryApplicationService} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationInvalidationOutboxQueryApplicationService Outbox 只读查询")
class AuthorizationInvalidationOutboxQueryApplicationServiceTest {

	@Mock
	private InvalidationOutboxQueryPort invalidationOutboxQueryPort;

	@InjectMocks
	private AuthorizationInvalidationOutboxQueryApplicationService queryApplicationService;

	private static AuthorizationInvalidationOutboxPageRowPO buildPageRow(Long id, String eventId, String status) {
		AuthorizationInvalidationOutboxPageRowPO row = new AuthorizationInvalidationOutboxPageRowPO();
		row.setId(id);
		row.setEventId(eventId);
		row.setChangeKind("ROLE");
		row.setStatus(status);
		row.setRetryCount(0);
		row.setMaxRetry(5);
		row.setCreateTime(LocalDateTime.now());
		return row;
	}

	@Test
	@DisplayName("分页查询：应返回 Outbox 分页结果")
	void getPage_shouldReturnPageResponse() {
		AuthorizationInvalidationOutboxQuery query = new AuthorizationInvalidationOutboxQuery();
		query.setPageIndex(1);
		query.setPageSize(10);

		AuthorizationInvalidationOutboxPageRowPO row = buildPageRow(1L, "evt-1", "SUCCESS");
		Page<AuthorizationInvalidationOutboxPageRowPO> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(row));
		when(invalidationOutboxQueryPort.selectPage(any(), any())).thenReturn(page);

		PageResponse<AuthorizationInvalidationOutboxPageVO> response = queryApplicationService.getPage(query);

		assertEquals(1L, response.getTotal());
		assertEquals("evt-1", response.getList().get(0).getEventId());
		assertEquals("SUCCESS", response.getList().get(0).getStatus());
		verify(invalidationOutboxQueryPort).selectPage(any(), any());
	}

	@Test
	@DisplayName("详情查询：记录不存在时抛出 DATA_NOT_EXIST")
	void getDetail_shouldThrowWhenMissing() {
		when(invalidationOutboxQueryPort.selectDetailById(99L)).thenReturn(null);

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> queryApplicationService.getDetail(99L));

		assertEquals(DATA_NOT_EXIST, exception.getResultCode());
	}

	@Test
	@DisplayName("详情查询：存在记录时返回详情 VO")
	void getDetail_shouldReturnDetailVo() {
		AuthorizationInvalidationOutboxDetailRowPO detailRow = new AuthorizationInvalidationOutboxDetailRowPO();
		detailRow.setId(10L);
		detailRow.setEventId("evt-detail");
		detailRow.setChangeKind("ROLE");
		detailRow.setStatus("DEAD");
		detailRow.setPayload("{\"roleCode\":\"ADMIN\"}");
		detailRow.setLastError("auth invalidate failed");
		when(invalidationOutboxQueryPort.selectDetailById(10L)).thenReturn(detailRow);

		AuthorizationInvalidationOutboxDetailVO detailVO = queryApplicationService.getDetail(10L);

		assertEquals("evt-detail", detailVO.getEventId());
		assertEquals("DEAD", detailVO.getStatus());
		assertEquals("{\"roleCode\":\"ADMIN\"}", detailVO.getPayload());
	}

}
