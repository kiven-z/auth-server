package com.auth.service.auth.application.authorization.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.application.port.invalidation.InvalidationEventQueryPort;
import com.auth.service.auth.domain.authorization.invalidation.model.InvalidationProcessingMarker;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventDetailRowPO;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.domain.authorization.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.auth.domain.authorization.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.domain.authorization.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.domain.authorization.model.vo.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.domain.authorization.model.vo.AuthorizationInvalidationEventStatsVO;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationEventQueryApplicationService} 单元测试
 */
@DisplayName("AuthorizationInvalidationEventQueryApplicationService 幂等事件只读查询")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationEventQueryApplicationServiceTest {

	@Mock
	private InvalidationEventQueryPort invalidationEventQueryPort;

	@InjectMocks
	private AuthorizationInvalidationEventQueryApplicationService queryApplicationService;

	private static AuthorizationInvalidationEventPageRowPO buildPageRow(Long id, String eventId,
			Integer impactedUserCount) {
		AuthorizationInvalidationEventPageRowPO row = new AuthorizationInvalidationEventPageRowPO();
		row.setId(id);
		row.setEventId(eventId);
		row.setChangeKind("ROLE");
		row.setImpactedUserCount(impactedUserCount);
		row.setProcessedAt(LocalDateTime.now());
		return row;
	}

	@Test
	@DisplayName("分页查询：应映射 processing 标记并返回分页结果")
	void getPage_shouldMapProcessingFlag() {
		// 构造已完成与处理中两条记录
		AuthorizationInvalidationEventQuery query = new AuthorizationInvalidationEventQuery();
		query.setPageIndex(1);
		query.setPageSize(10);

		AuthorizationInvalidationEventPageRowPO completedRow = buildPageRow(1L, "evt-1", 3);
		AuthorizationInvalidationEventPageRowPO processingRow = buildPageRow(2L, "evt-2",
				InvalidationProcessingMarker.PROCESSING_COUNT);

		Page<AuthorizationInvalidationEventPageRowPO> page = new Page<>(1, 10, 2);
		page.setRecords(List.of(completedRow, processingRow));
		when(invalidationEventQueryPort.selectPage(any(), any())).thenReturn(page);

		PageResponse<AuthorizationInvalidationEventPageVO> response = queryApplicationService.getPage(query);

		assertEquals(2L, response.getTotal());
		assertFalse(response.getList().get(0).getProcessing());
		assertTrue(response.getList().get(1).getProcessing());
		verify(invalidationEventQueryPort).selectPage(any(), any());
	}

	@Test
	@DisplayName("详情查询：记录不存在时抛出 DATA_NOT_EXIST")
	void getDetail_shouldThrowWhenMissing() {
		when(invalidationEventQueryPort.selectDetailById(99L)).thenReturn(null);

		AuthBusinessException exception = assertThrows(AuthBusinessException.class,
				() -> queryApplicationService.getDetail(99L));

		assertEquals(AuthResultCode.DATA_NOT_EXIST, exception.getResultCode());
	}

	@Test
	@DisplayName("详情查询：存在记录时返回详情 VO")
	void getDetail_shouldReturnDetailVo() {
		AuthorizationInvalidationEventDetailRowPO detailRow = new AuthorizationInvalidationEventDetailRowPO();
		detailRow.setId(10L);
		detailRow.setEventId("evt-detail");
		detailRow.setChangeKind("ROLE");
		detailRow.setImpactedUserCount(5);
		detailRow.setProcessedAt(LocalDateTime.now());
		when(invalidationEventQueryPort.selectDetailById(10L)).thenReturn(detailRow);

		AuthorizationInvalidationEventDetailVO detailVO = queryApplicationService.getDetail(10L);

		assertEquals("evt-detail", detailVO.getEventId());
		assertEquals("ROLE", detailVO.getChangeKind());
		assertFalse(detailVO.getProcessing());
	}

	@Test
	@DisplayName("统计查询：PO 为空时应归零")
	void getStats_shouldReturnZeroWhenPoNull() {
		when(invalidationEventQueryPort.selectEventStats()).thenReturn(null);

		AuthorizationInvalidationEventStatsVO statsVo = queryApplicationService.getStats();

		assertEquals(0L, statsVo.getTotalCount());
		assertEquals(0L, statsVo.getProcessingCount());
		assertEquals(0L, statsVo.getCompletedCount());
	}

	@Test
	@DisplayName("统计查询：计数字段为空时应归零")
	void getStats_shouldDefaultNullCountsToZero() {
		when(invalidationEventQueryPort.selectEventStats()).thenReturn(new AuthorizationInvalidationEventStatsPO());

		AuthorizationInvalidationEventStatsVO statsVo = queryApplicationService.getStats();

		assertEquals(0L, statsVo.getTotalCount());
		assertEquals(0L, statsVo.getProcessingCount());
		assertEquals(0L, statsVo.getCompletedCount());
	}

	@Test
	@DisplayName("统计查询：应返回幂等事件处理状态计数")
	void getStats_shouldReturnEventStats() {
		AuthorizationInvalidationEventStatsPO statsPo = new AuthorizationInvalidationEventStatsPO();
		statsPo.setTotalCount(12L);
		statsPo.setProcessingCount(1L);
		statsPo.setCompletedCount(11L);
		when(invalidationEventQueryPort.selectEventStats()).thenReturn(statsPo);

		AuthorizationInvalidationEventStatsVO statsVo = queryApplicationService.getStats();

		assertEquals(12L, statsVo.getTotalCount());
		assertEquals(1L, statsVo.getProcessingCount());
		assertEquals(11L, statsVo.getCompletedCount());
		verify(invalidationEventQueryPort).selectEventStats();
	}

}
