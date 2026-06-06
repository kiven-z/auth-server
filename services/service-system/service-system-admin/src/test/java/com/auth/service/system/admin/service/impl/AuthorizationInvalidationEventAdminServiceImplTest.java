package com.auth.service.system.admin.service.impl;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.admin.model.vo.authorizationinvalidation.AuthorizationInvalidationEventPageVO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.feign.AuthInnerFeignClient;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventDetailInnerDTO;
import com.auth.service.system.common.feign.dto.AuthorizationInvalidationEventPageInnerDTO;
import com.auth.service.system.common.service.AuditUserDisplayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.auth.service.system.admin.exception.SystemAdminResultCode.AUTH_INVALIDATION_EVENT_RELEASE_NOT_PROCESSING;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationEventAdminServiceImpl} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationInvalidationEventAdminServiceImpl 幂等事件管理端查询")
class AuthorizationInvalidationEventAdminServiceImplTest {

	@Mock
	private AuthInnerFeignClient authInnerFeignClient;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@InjectMocks
	private AuthorizationInvalidationEventAdminServiceImpl adminService;

	@Test
	@DisplayName("分页查询：Feign 失败时抛出 SERVICE_UNAVAILABLE")
	void getPage_shouldThrowWhenFeignFailed() {
		when(authInnerFeignClient.getInvalidationEventPage(org.mockito.ArgumentMatchers.any()))
			.thenReturn(Result.error("unavailable"));

		AuthorizationInvalidationEventQuery query = new AuthorizationInvalidationEventQuery();
		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> adminService.getPage(query));

		assertEquals(SERVICE_UNAVAILABLE, exception.getResultCode());
	}

	@Test
	@DisplayName("分页查询：Feign 成功时应转换并 enrich 审计用户名")
	void getPage_shouldConvertAndEnrich() {
		AuthorizationInvalidationEventPageInnerDTO innerDto = new AuthorizationInvalidationEventPageInnerDTO();
		innerDto.setId(1L);
		innerDto.setEventId("evt-1");
		innerDto.setChangeKind("ROLE");
		innerDto.setProcessing(Boolean.FALSE);

		PageResponse<AuthorizationInvalidationEventPageInnerDTO> pageData = PageResponse.of(1L, 10L, 1L,
				List.of(innerDto));
		when(authInnerFeignClient.getInvalidationEventPage(org.mockito.ArgumentMatchers.any()))
			.thenReturn(Result.success(pageData));

		PageResponse<AuthorizationInvalidationEventPageVO> response = adminService
			.getPage(new AuthorizationInvalidationEventQuery());

		assertEquals(1L, response.getTotal());
		assertEquals("evt-1", response.getList().get(0).getEventId());
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("详情查询：Feign 无数据时抛出 DATA_NOT_EXIST")
	void getDetail_shouldThrowWhenFeignFailed() {
		when(authInnerFeignClient.getInvalidationEventDetail(1L)).thenReturn(Result.error("not found"));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> adminService.getDetail(1L));

		assertEquals(DATA_NOT_EXIST, exception.getResultCode());
	}

	@Test
	@DisplayName("详情查询：Feign 成功时返回详情 VO")
	void getDetail_shouldReturnDetailVo() {
		AuthorizationInvalidationEventDetailInnerDTO innerDto = new AuthorizationInvalidationEventDetailInnerDTO();
		innerDto.setId(2L);
		innerDto.setEventId("evt-detail");
		when(authInnerFeignClient.getInvalidationEventDetail(2L)).thenReturn(Result.success(innerDto));

		AuthorizationInvalidationEventDetailVO detailVO = adminService.getDetail(2L);

		assertEquals("evt-detail", detailVO.getEventId());
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
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
				() -> adminService.releaseProcessingClaim(3L));

		assertEquals(AUTH_INVALIDATION_EVENT_RELEASE_NOT_PROCESSING, exception.getResultCode());
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

		boolean released = adminService.releaseProcessingClaim(4L);

		assertTrue(released);
		verify(authInnerFeignClient).releaseInvalidationEventClaim("evt-processing");
	}

}
