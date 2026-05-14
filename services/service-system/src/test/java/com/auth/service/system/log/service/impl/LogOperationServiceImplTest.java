package com.auth.service.system.log.service.impl;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.mapper.LogOperationMapper;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.auth.service.system.log.model.po.LogOperationPageRowPO;
import com.auth.service.system.log.model.query.LogOperationQuery;
import com.auth.service.system.log.model.vo.LogOperationDetailVO;
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
 * {@link LogOperationServiceImpl} 单元测试
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class LogOperationServiceImplTest {

	@Mock
	private LogOperationMapper logOperationMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private LogOperationServiceImpl logOperationService;

	@BeforeEach
	void setUp() {
		logOperationService = new LogOperationServiceImpl(auditUserDisplayService);
		ReflectionTestUtils.setField(logOperationService, "baseMapper", logOperationMapper);
	}

	@Test
	@DisplayName("详情：记录不存在时抛出业务异常")
	void getDetail_whenMissing_throws() {
		when(logOperationMapper.selectById(1L)).thenReturn(null);

		assertThrows(SystemBusinessException.class, () -> logOperationService.getDetail(1L));
	}

	@Test
	@DisplayName("详情：逻辑删除记录抛出业务异常")
	void getDetail_whenSoftDeleted_throws() {
		LogOperationEntity entity = new LogOperationEntity();
		entity.setId(2L);
		entity.setIsDeleted(true);
		when(logOperationMapper.selectById(2L)).thenReturn(entity);

		assertThrows(SystemBusinessException.class, () -> logOperationService.getDetail(2L));
	}

	@Test
	@DisplayName("详情：存在且未删除时返回 VO 并填充审计用户名")
	void getDetail_whenPresent_returnsVo() {
		LogOperationEntity entity = new LogOperationEntity();
		entity.setId(9L);
		entity.setUserId(100L);
		entity.setModule("SYSTEM:USER");
		entity.setOperationType("QUERY");
		entity.setIsDeleted(false);
		when(logOperationMapper.selectById(9L)).thenReturn(entity);

		LogOperationDetailVO vo = logOperationService.getDetail(9L);

		assertNotNull(vo);
		assertEquals(9L, vo.getId());
		assertEquals(100L, vo.getUserId());
		assertEquals("SYSTEM:USER", vo.getModule());
		assertEquals("QUERY", vo.getOperationType());
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class));
	}

	@Test
	@DisplayName("分页：应调用 Mapper、操作用户名解析与审计用户名填充")
	void getPage_invokesMapperAndEnrichment() {
		LogOperationQuery query = new LogOperationQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		Page<LogOperationEntity> page = new Page<>(1, 10);
		IPage<LogOperationPageRowPO> mockPage = new Page<>(1, 10, 0);
		mockPage.setRecords(List.of());
		when(logOperationMapper.selectListByPage(any(Page.class), any(LogOperationQuery.class))).thenReturn(mockPage);

		var response = logOperationService.getPage(page, query);

		assertNotNull(response);
		verify(logOperationMapper).selectListByPage(any(Page.class), any(LogOperationQuery.class));
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class));
	}

}
