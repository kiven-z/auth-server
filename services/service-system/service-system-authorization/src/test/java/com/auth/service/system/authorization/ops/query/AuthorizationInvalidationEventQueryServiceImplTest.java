package com.auth.service.system.authorization.ops.query;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationEventDetailVO;
import com.auth.service.system.authorization.ops.model.vo.AuthorizationInvalidationEventPageVO;
import com.auth.service.system.authorization.ops.exception.AuthorizationInvalidationOpsResultCode;
import com.auth.service.system.authorization.ops.model.query.AuthorizationInvalidationEventOpsQuery;
import com.auth.service.system.authorization.ops.service.AuthorizationInvalidationEventOpsService;
import com.auth.service.system.common.exception.SystemBusinessException;
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

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthorizationInvalidationEventQueryServiceImpl} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationInvalidationEventQueryServiceImpl 幂等事件运维查询")
class AuthorizationInvalidationEventQueryServiceImplTest {

	@Mock
	private AuthorizationInvalidationEventOpsService authorizationInvalidationEventOpsService;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@InjectMocks
	private AuthorizationInvalidationEventQueryServiceImpl queryService;

	@Test
	@DisplayName("分页查询：运维门面失败时抛出 SERVICE_UNAVAILABLE")
	void getPage_shouldThrowWhenOpsFailed() {
		when(authorizationInvalidationEventOpsService.getPage(any(AuthorizationInvalidationEventOpsQuery.class)))
			.thenThrow(new SystemBusinessException(SERVICE_UNAVAILABLE));

		AuthorizationInvalidationEventQuery query = new AuthorizationInvalidationEventQuery();
		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> queryService.getPage(query));

		assertEquals(SERVICE_UNAVAILABLE, exception.getResultCode());
	}

	@Test
	@DisplayName("分页查询：运维门面成功时应转换并 enrich 审计用户名")
	void getPage_shouldConvertAndEnrich() {
		AuthorizationInvalidationEventPageInnerDTO innerDto = new AuthorizationInvalidationEventPageInnerDTO();
		innerDto.setId(1L);
		innerDto.setEventId("evt-1");
		innerDto.setChangeKind("ROLE");
		innerDto.setProcessing(Boolean.FALSE);

		PageResponse<AuthorizationInvalidationEventPageInnerDTO> pageData = PageResponse.of(1L, 10L, 1L,
				List.of(innerDto));
		when(authorizationInvalidationEventOpsService.getPage(any(AuthorizationInvalidationEventOpsQuery.class)))
			.thenReturn(pageData);

		PageResponse<AuthorizationInvalidationEventPageVO> response = queryService
			.getPage(new AuthorizationInvalidationEventQuery());

		assertEquals(1L, response.getTotal());
		assertEquals("evt-1", response.getList().get(0).getEventId());
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("详情查询：运维门面无数据时抛出 DATA_NOT_EXIST")
	void getDetail_shouldThrowWhenOpsFailed() {
		when(authorizationInvalidationEventOpsService.getDetail(1L))
			.thenThrow(new SystemBusinessException(DATA_NOT_EXIST));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> queryService.getDetail(1L));

		assertEquals(DATA_NOT_EXIST, exception.getResultCode());
	}

	@Test
	@DisplayName("详情查询：运维门面成功时返回详情 VO")
	void getDetail_shouldReturnDetailVo() {
		AuthorizationInvalidationEventDetailInnerDTO innerDto = new AuthorizationInvalidationEventDetailInnerDTO();
		innerDto.setId(2L);
		innerDto.setEventId("evt-detail");
		when(authorizationInvalidationEventOpsService.getDetail(2L)).thenReturn(innerDto);

		AuthorizationInvalidationEventDetailVO detailVO = queryService.getDetail(2L);

		assertEquals("evt-detail", detailVO.getEventId());
		verify(auditUserDisplayService).enrichAuditUsernames(anyList());
	}

	@Test
	@DisplayName("释放占位：应委托运维门面")
	void releaseProcessingClaim_shouldDelegateToOps() {
		when(authorizationInvalidationEventOpsService.releaseProcessingClaim(4L)).thenReturn(true);

		boolean released = queryService.releaseProcessingClaim(4L);

		assertTrue(released);
		verify(authorizationInvalidationEventOpsService).releaseProcessingClaim(4L);
	}

	@Test
	@DisplayName("释放占位：非 processing 记录应透传运维结果码")
	void releaseProcessingClaim_shouldPropagateOpsResultCode() {
		when(authorizationInvalidationEventOpsService.releaseProcessingClaim(3L)).thenThrow(
				new SystemBusinessException(AuthorizationInvalidationOpsResultCode.EVENT_RELEASE_NOT_PROCESSING));

		SystemBusinessException exception = assertThrows(SystemBusinessException.class,
				() -> queryService.releaseProcessingClaim(3L));

		assertEquals(AuthorizationInvalidationOpsResultCode.EVENT_RELEASE_NOT_PROCESSING, exception.getResultCode());
	}

}
