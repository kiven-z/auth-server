package com.auth.service.system.log.service.impl;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.mapper.LogAuthorizationAuditMapper;
import com.auth.service.system.log.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.log.model.po.LogAuthorizationAuditDetailRowPO;
import com.auth.service.system.log.model.po.LogAuthorizationAuditPageRowPO;
import com.auth.service.system.log.model.query.LogAuthorizationAuditQuery;
import com.auth.service.system.log.model.vo.LogAuthorizationAuditDetailVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LogAuthorizationAuditServiceImpl} 单元测试
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class LogAuthorizationAuditServiceImplTest {

	@Mock
	private LogAuthorizationAuditMapper logAuthorizationAuditMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private LogAuthorizationAuditServiceImpl logAuthorizationAuditService;

	@BeforeEach
	void setUp() {
		logAuthorizationAuditService = new LogAuthorizationAuditServiceImpl(auditUserDisplayService);
		ReflectionTestUtils.setField(logAuthorizationAuditService, "baseMapper", logAuthorizationAuditMapper);
	}

	@Test
	@DisplayName("详情：记录不存在时抛出业务异常")
	void getDetail_whenMissing_throws() {
		// 模拟 Mapper 未查到行
		when(logAuthorizationAuditMapper.selectDetailById(1L)).thenReturn(null);

		assertThrows(SystemBusinessException.class, () -> logAuthorizationAuditService.getDetail(1L));
	}

	@Test
	@DisplayName("详情：存在时返回 VO")
	void getDetail_whenPresent_returnsVo() {
		// 构造持久层投影
		LogAuthorizationAuditDetailRowPO po = new LogAuthorizationAuditDetailRowPO();
		po.setId(9L);
		po.setEventType("GRANTED");
		po.setDecisionReason("MATCHED");
		when(logAuthorizationAuditMapper.selectDetailById(9L)).thenReturn(po);

		LogAuthorizationAuditDetailVO vo = logAuthorizationAuditService.getDetail(9L);

		assertNotNull(vo);
		assertEquals(9L, vo.getId());
		assertEquals("GRANTED", vo.getEventType());
		assertEquals("MATCHED", vo.getDecisionReason());
	}

	@Test
	@DisplayName("分页：应调用 Mapper 并填充审计用户名")
	void getPage_invokesMapperAndEnrichment() {
		// 准备分页参数与空结果页
		LogAuthorizationAuditQuery query = new LogAuthorizationAuditQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		Page<LogAuthorizationAuditEntity> page = new Page<>(1, 10);
		IPage<LogAuthorizationAuditPageRowPO> mockPage = new Page<>(1, 10, 0);
		mockPage.setRecords(List.of());
		when(logAuthorizationAuditMapper.selectListByPage(any(Page.class), any(LogAuthorizationAuditQuery.class)))
			.thenReturn(mockPage);

		var response = logAuthorizationAuditService.getPage(page, query);

		assertNotNull(response);
		verify(logAuthorizationAuditMapper).selectListByPage(any(Page.class), any(LogAuthorizationAuditQuery.class));
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class));
	}

}
