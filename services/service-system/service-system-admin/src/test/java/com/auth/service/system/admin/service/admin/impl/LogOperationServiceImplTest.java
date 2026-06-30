package com.auth.service.system.admin.service.admin.impl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.ToLongFunction;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.mapper.log.LogOperationMapper;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import com.auth.service.system.admin.model.po.logoperation.LogOperationPageRowPO;
import com.auth.service.system.admin.model.query.log.LogOperationQuery;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationDetailVO;
import com.auth.service.system.admin.model.vo.logoperation.LogOperationPageVO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LogOperationQueryServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("LogOperationQueryServiceImpl 操作日志")
@ExtendWith(MockitoExtension.class)
class LogOperationQueryServiceImplTest {

	@Mock
	private LogOperationMapper logOperationMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private LogOperationQueryServiceImpl logOperationQueryService;

	@BeforeEach
	void setUp() throws Exception {
		logOperationQueryService = new LogOperationQueryServiceImpl(auditUserDisplayService);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(logOperationQueryService, logOperationMapper);
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
		doAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			IPage<LogOperationPageVO> page = invocation.getArgument(0);
			ToLongFunction<LogOperationPageVO> userIdGetter = invocation.getArgument(1);
			BiConsumer<LogOperationPageVO, String> usernameSetter = invocation.getArgument(2);
			for (LogOperationPageVO vo : page.getRecords()) {
				long userId = userIdGetter.applyAsLong(vo);
				if (userId != 0L) {
					usernameSetter.accept(vo, "admin");
				}
			}
			return null;
		}).when(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), any(), any());

		PageResponse<LogOperationPageVO> result = logOperationQueryService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		assertThat(result.getList().get(0).getUsername()).isEqualTo("admin");
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), any(), any());
		verify(auditUserDisplayService, never()).mapUsernamesByIds(any());
	}

	@Test
	@DisplayName("详情查询：填充操作用户名")
	void getDetailEnrichesOperatorUsername() {
		LogOperationEntity entity = new LogOperationEntity();
		entity.setId(10L);
		entity.setUserId(2L);
		when(logOperationMapper.selectById(10L)).thenReturn(entity);
		doAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			List<LogOperationDetailVO> records = invocation.getArgument(0);
			ToLongFunction<LogOperationDetailVO> userIdGetter = invocation.getArgument(1);
			BiConsumer<LogOperationDetailVO, String> usernameSetter = invocation.getArgument(2);
			for (LogOperationDetailVO vo : records) {
				long userId = userIdGetter.applyAsLong(vo);
				if (userId != 0L) {
					usernameSetter.accept(vo, "operator");
				}
			}
			return null;
		}).when(auditUserDisplayService).enrichAuditUsernames(any(List.class), any(), any());

		LogOperationDetailVO detail = logOperationQueryService.getDetail(10L);

		assertThat(detail.getUsername()).isEqualTo("operator");
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class), any(), any());
		verify(auditUserDisplayService, never()).mapUsernamesByIds(any());
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

		PageResponse<LogOperationPageVO> result = logOperationQueryService.getPage(pageParams, query);

		assertThat(result.getList()).hasSize(1);
		assertThat(result.getList().get(0).getUserId()).isNull();
		assertThat(result.getList().get(0).getUsername()).isNull();
	}

	@Test
	@DisplayName("详情查询：记录不存在时抛出业务异常")
	void getDetailThrowsWhenNotFound() {
		when(logOperationMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> logOperationQueryService.getDetail(99L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

}
