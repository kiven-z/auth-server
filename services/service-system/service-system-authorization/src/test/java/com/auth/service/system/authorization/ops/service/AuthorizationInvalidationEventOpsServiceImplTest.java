package com.auth.service.system.authorization.ops.service;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.ops.exception.AuthorizationInvalidationOpsResultCode;
import com.auth.service.system.authorization.ops.model.po.AuthorizationInvalidationEventStatsPO;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationEventOpsQuery;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.authorization.integration.feign.AuthInnerFeignClient;
import com.auth.service.system.authorization.integration.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.authorization.integration.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.authorization.integration.feign.dto.AuthorizationInvalidationEventStatsInnerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationEventOpsServiceImpl} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationInvalidationEventOpsServiceImpl 幂等事件运维")
class AuthorizationInvalidationEventOpsServiceImplTest {

	@Mock
	private AuthInnerFeignClient authInnerFeignClient;

	private AuthorizationInvalidationEventOpsServiceImpl opsService;

	@BeforeEach
	void setUpService() {
		opsService = new AuthorizationInvalidationEventOpsServiceImpl(authInnerFeignClient);
	}

	@Test
	@DisplayName("分页查询：Feign 失败时抛出 SERVICE_UNAVAILABLE")
	void getPage_shouldThrowWhenFeignFailed() {
		when(authInnerFeignClient.getInvalidationEventPage(any())).thenReturn(Result.error("unavailable"));
		AuthorizationInvalidationEventOpsQuery query = new AuthorizationInvalidationEventOpsQuery();

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> opsService.getPage(query));

		assertEquals(SERVICE_UNAVAILABLE, exception.getResultCode());
	}

	@Test
	@DisplayName("分页查询：Feign 成功时返回分页数据")
	void getPage_shouldReturnPageData() {
		AuthorizationInvalidationEventPageInnerDTO innerDto = new AuthorizationInvalidationEventPageInnerDTO();
		innerDto.setId(1L);
		innerDto.setEventId("evt-1");
		PageResponse<AuthorizationInvalidationEventPageInnerDTO> pageData = PageResponse.of(1L, 10L, 1L,
				List.of(innerDto));
		when(authInnerFeignClient.getInvalidationEventPage(any())).thenReturn(Result.success(pageData));

		PageResponse<AuthorizationInvalidationEventPageInnerDTO> response = opsService
			.getPage(new AuthorizationInvalidationEventOpsQuery());

		assertEquals(1L, response.getTotal());
		assertEquals("evt-1", response.getList().get(0).getEventId());
	}

	@Test
	@DisplayName("详情查询：Feign 无数据时抛出 DATA_NOT_EXIST")
	void getDetail_shouldThrowWhenFeignFailed() {
		when(authInnerFeignClient.getInvalidationEventDetail(1L)).thenReturn(Result.error("not found"));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class, () -> opsService.getDetail(1L));

		assertEquals(DATA_NOT_EXIST, exception.getResultCode());
	}

	@Test
	@DisplayName("统计摘要：Feign 失败时抛出 SERVICE_UNAVAILABLE")
	void getEventStats_shouldThrowWhenFeignFailed() {
		when(authInnerFeignClient.getInvalidationEventSummary()).thenReturn(Result.error("unavailable"));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class, opsService::getEventStats);

		assertEquals(SERVICE_UNAVAILABLE, exception.getResultCode());
	}

	@Test
	@DisplayName("统计摘要：Feign 成功时映射统计 PO")
	void getEventStats_shouldMapStatsPo() {
		AuthorizationInvalidationEventStatsInnerDTO statsDto = new AuthorizationInvalidationEventStatsInnerDTO();
		statsDto.setTotalCount(8L);
		statsDto.setProcessingCount(1L);
		statsDto.setCompletedCount(7L);
		when(authInnerFeignClient.getInvalidationEventSummary()).thenReturn(Result.success(statsDto));

		AuthorizationInvalidationEventStatsPO statsPo = opsService.getEventStats();

		assertEquals(8L, statsPo.getTotalCount());
		assertEquals(1L, statsPo.getProcessingCount());
		assertEquals(7L, statsPo.getCompletedCount());
	}

	@Test
	@DisplayName("释放占位：非 processing 记录应拒绝")
	void releaseProcessingClaim_shouldRejectCompletedEvent() {
		AuthorizationInvalidationEventDetailInnerDTO innerDto = new AuthorizationInvalidationEventDetailInnerDTO();
		innerDto.setId(3L);
		innerDto.setEventId("evt-done");
		innerDto.setProcessing(Boolean.FALSE);
		when(authInnerFeignClient.getInvalidationEventDetail(3L)).thenReturn(Result.success(innerDto));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> opsService.releaseProcessingClaim(3L));

		assertEquals(AuthorizationInvalidationOpsResultCode.EVENT_RELEASE_NOT_PROCESSING, exception.getResultCode());
		verify(authInnerFeignClient, never()).releaseInvalidationEventClaim("evt-done");
	}

	@Test
	@DisplayName("释放占位：processing 记录应调用 Feign 释放")
	void releaseProcessingClaim_shouldCallFeignRelease() {
		AuthorizationInvalidationEventDetailInnerDTO innerDto = new AuthorizationInvalidationEventDetailInnerDTO();
		innerDto.setId(4L);
		innerDto.setEventId("evt-processing");
		innerDto.setProcessing(Boolean.TRUE);
		when(authInnerFeignClient.getInvalidationEventDetail(4L)).thenReturn(Result.success(innerDto));
		when(authInnerFeignClient.releaseInvalidationEventClaim("evt-processing")).thenReturn(Result.success(true));

		boolean released = opsService.releaseProcessingClaim(4L);

		assertTrue(released);
		verify(authInnerFeignClient).releaseInvalidationEventClaim("evt-processing");
	}

}
