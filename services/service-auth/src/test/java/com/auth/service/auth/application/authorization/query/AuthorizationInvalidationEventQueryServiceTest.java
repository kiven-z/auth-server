package com.auth.service.auth.application.authorization.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.auth.api.dto.authorization.request.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventDetailVO;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventPageVO;
import com.auth.service.auth.api.dto.authorization.response.AuthorizationInvalidationEventStatsVO;
import com.auth.service.auth.domain.authorization.model.InvalidationProcessingMarker;
import com.auth.service.auth.infrastructure.persistence.mapper.AuthorizationInvalidationEventMapper;
import com.auth.service.auth.infrastructure.persistence.mapper.InvalidationEventPageFilter;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventDetailRow;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventPageRow;
import com.auth.service.auth.infrastructure.persistence.projection.AuthorizationInvalidationEventStatsRow;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * {@link AuthorizationInvalidationEventQueryService} 单元测试
 */
@DisplayName("AuthorizationInvalidationEventQueryService 幂等事件只读查询")
@ExtendWith(MockitoExtension.class)
class AuthorizationInvalidationEventQueryServiceTest {

	@Mock
	private AuthorizationInvalidationEventMapper authorizationInvalidationEventMapper;

	@InjectMocks
	private AuthorizationInvalidationEventQueryService queryService;

	private static AuthorizationInvalidationEventPageRow buildPageRow(Long id, String eventId,
			Integer impactedUserCount) {
		AuthorizationInvalidationEventPageRow row = new AuthorizationInvalidationEventPageRow();
		row.setId(id);
		row.setEventId(eventId);
		row.setChangeKind("ROLE");
		row.setImpactedUserCount(impactedUserCount);
		row.setProcessedAt(LocalDateTime.now());
		return row;
	}

	@Test
	@DisplayName("分页查询：应调用 Mapper 并返回分页 VO")
	void getPage_shouldQueryMapperAndReturnPageVo() {
		AuthorizationInvalidationEventQuery query = new AuthorizationInvalidationEventQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		query.setEventId("evt-prefix");
		query.setProcessing(true);

		AuthorizationInvalidationEventPageRow completedRow = buildPageRow(1L, "evt-1", 3);
		AuthorizationInvalidationEventPageRow processingRow = buildPageRow(2L, "evt-2",
				InvalidationProcessingMarker.PROCESSING_COUNT);

		Page<AuthorizationInvalidationEventPageRow> pageResult = new Page<>(1, 10, 2);
		pageResult.setRecords(List.of(completedRow, processingRow));
		when(authorizationInvalidationEventMapper.selectListByPage(any(Page.class),
				any(InvalidationEventPageFilter.class)))
			.thenReturn(pageResult);

		PageResponse<AuthorizationInvalidationEventPageVO> response = queryService.getPage(query);

		assertEquals(2L, response.getTotal());
		assertEquals("evt-1", response.getList().get(0).getEventId());
		assertEquals(3, response.getList().get(0).getImpactedUserCount());
		assertFalse(response.getList().get(0).getProcessing());
		assertTrue(response.getList().get(1).getProcessing());

		ArgumentCaptor<InvalidationEventPageFilter> filterCaptor = ArgumentCaptor
			.forClass(InvalidationEventPageFilter.class);
		verify(authorizationInvalidationEventMapper).selectListByPage(any(Page.class), filterCaptor.capture());
		assertEquals("evt-prefix", filterCaptor.getValue().getEventId());
		assertTrue(filterCaptor.getValue().getProcessing());
	}

	@Test
	@DisplayName("详情查询：记录不存在时抛出 DATA_NOT_EXIST")
	void getDetail_shouldThrowWhenMissing() {
		when(authorizationInvalidationEventMapper.selectDetailById(99L)).thenReturn(null);

		AuthBusinessException exception = assertThrows(AuthBusinessException.class, () -> queryService.getDetail(99L));

		assertEquals(AuthResultCode.DATA_NOT_EXIST, exception.getResultCode());
	}

	@Test
	@DisplayName("详情查询：存在记录时返回详情 VO")
	void getDetail_shouldReturnDetailVo() {
		AuthorizationInvalidationEventDetailRow row = new AuthorizationInvalidationEventDetailRow();
		row.setId(10L);
		row.setEventId("evt-detail");
		row.setChangeKind("ROLE");
		row.setImpactedUserCount(5);
		row.setProcessedAt(LocalDateTime.now());
		when(authorizationInvalidationEventMapper.selectDetailById(10L)).thenReturn(row);

		AuthorizationInvalidationEventDetailVO result = queryService.getDetail(10L);

		assertEquals("evt-detail", result.getEventId());
		assertEquals("ROLE", result.getChangeKind());
		assertEquals(5, result.getImpactedUserCount());
		assertFalse(result.getProcessing());
	}

	@Test
	@DisplayName("统计查询：无数据时各计数归零")
	void getStats_shouldReturnZeroWhenRowNull() {
		when(authorizationInvalidationEventMapper.selectEventStats()).thenReturn(null);

		AuthorizationInvalidationEventStatsVO result = queryService.getStats();

		assertEquals(0L, result.getTotalCount());
		assertEquals(0L, result.getProcessingCount());
		assertEquals(0L, result.getCompletedCount());
	}

	@Test
	@DisplayName("统计查询：计数字段为空时归零")
	void getStats_shouldDefaultNullCountsToZero() {
		when(authorizationInvalidationEventMapper.selectEventStats())
			.thenReturn(new AuthorizationInvalidationEventStatsRow());

		AuthorizationInvalidationEventStatsVO result = queryService.getStats();

		assertEquals(0L, result.getTotalCount());
		assertEquals(0L, result.getProcessingCount());
		assertEquals(0L, result.getCompletedCount());
	}

	@Test
	@DisplayName("统计查询：应调用 Mapper 并返回统计 VO")
	void getStats_shouldReturnEventStatsVo() {
		AuthorizationInvalidationEventStatsRow row = new AuthorizationInvalidationEventStatsRow();
		row.setTotalCount(12L);
		row.setProcessingCount(1L);
		row.setCompletedCount(11L);
		when(authorizationInvalidationEventMapper.selectEventStats()).thenReturn(row);

		AuthorizationInvalidationEventStatsVO result = queryService.getStats();

		assertEquals(12L, result.getTotalCount());
		assertEquals(1L, result.getProcessingCount());
		assertEquals(11L, result.getCompletedCount());
		verify(authorizationInvalidationEventMapper).selectEventStats();
	}

}
