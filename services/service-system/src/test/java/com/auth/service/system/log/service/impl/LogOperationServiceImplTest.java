package com.auth.service.system.log.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.mapper.LogOperationMapper;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.auth.service.system.log.model.po.LogOperationPageRowPO;
import com.auth.service.system.log.model.query.LogOperationQuery;
import com.auth.service.system.log.model.vo.LogOperationDetailVO;
import com.auth.service.system.log.model.vo.LogOperationPageVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LogOperationServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("LogOperationServiceImpl 操作日志")
@ExtendWith(MockitoExtension.class)
class LogOperationServiceImplTest {

	@Mock
	private LogOperationMapper logOperationMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private LogOperationServiceImpl logOperationService;

	@BeforeEach
	void setUp() throws Exception {
		logOperationService = new LogOperationServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(logOperationService, logOperationMapper);
	}

	@Test
	@DisplayName("分页查询：填充操作用户名")
	void getPageEnrichesOperatorUsername() {
		Page<LogOperationEntity> pageParams = new Page<>(1, 10);
		LogOperationQuery query = new LogOperationQuery();
		LogOperationPageRowPO po = new LogOperationPageRowPO();
		po.setUserId(1L);
		Page<LogOperationPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(po));
		when(logOperationMapper.selectListByPage(pageParams, query)).thenReturn(poPage);
		when(auditUserDisplayService.mapUsernamesByIds(argThat(ids -> ids.contains(1L))))
			.thenReturn(Map.of(1L, "admin"));

		PageResponse<LogOperationPageVO> result = logOperationService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		assertThat(result.getList().get(0).getUsername()).isEqualTo("admin");
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class));
		verify(auditUserDisplayService).mapUsernamesByIds(argThat(ids -> ids.contains(1L)));
	}

	@Test
	@DisplayName("详情查询：填充操作用户名")
	void getDetailEnrichesOperatorUsername() {
		LogOperationEntity entity = new LogOperationEntity();
		entity.setId(10L);
		entity.setUserId(2L);
		when(logOperationMapper.selectById(10L)).thenReturn(entity);
		when(auditUserDisplayService.mapUsernamesByIds(List.of(2L))).thenReturn(Map.of(2L, "operator"));

		LogOperationDetailVO detail = logOperationService.getDetail(10L);

		assertThat(detail.getUsername()).isEqualTo("operator");
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class));
	}

	@Test
	@DisplayName("分页查询：匿名用户不解析用户名")
	void getPageSkipsUsernameWhenUserIdNull() {
		Page<LogOperationEntity> pageParams = new Page<>(1, 10);
		LogOperationQuery query = new LogOperationQuery();
		LogOperationPageRowPO po = new LogOperationPageRowPO();
		po.setUserId(null);
		Page<LogOperationPageRowPO> poPage = new Page<>(1, 10);
		poPage.setRecords(List.of(po));
		when(logOperationMapper.selectListByPage(pageParams, query)).thenReturn(poPage);

		PageResponse<LogOperationPageVO> result = logOperationService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		assertThat(result.getList().get(0).getUserId()).isNull();
		assertThat(result.getList().get(0).getUsername()).isNull();
	}

	@Test
	@DisplayName("详情查询：记录不存在时抛出业务异常")
	void getDetailThrowsWhenNotFound() {
		when(logOperationMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> logOperationService.getDetail(99L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

}
