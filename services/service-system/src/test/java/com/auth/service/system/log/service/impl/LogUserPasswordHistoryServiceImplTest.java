package com.auth.service.system.log.service.impl;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.log.mapper.LogUserPasswordHistoryMapper;
import com.auth.service.system.log.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.log.model.po.LogUserPasswordHistoryDetailRowPO;
import com.auth.service.system.log.model.query.LogUserPasswordHistoryQuery;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryDetailVO;
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

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LogUserPasswordHistoryServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("LogUserPasswordHistoryServiceImpl 密码历史日志")
@ExtendWith(MockitoExtension.class)
class LogUserPasswordHistoryServiceImplTest {

	@Mock
	private LogUserPasswordHistoryMapper logUserPasswordHistoryMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private LogUserPasswordHistoryServiceImpl logUserPasswordHistoryService;

	@BeforeEach
	void setUp() throws Exception {
		logUserPasswordHistoryService = new LogUserPasswordHistoryServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(logUserPasswordHistoryService, logUserPasswordHistoryMapper);
	}

	@Test
	@DisplayName("详情查询：存在记录时填充审计用户名")
	void getDetailEnrichesAuditUsernames() {
		LogUserPasswordHistoryDetailRowPO po = new LogUserPasswordHistoryDetailRowPO();
		po.setId(1L);
		po.setUserId(10L);
		po.setCreateUser(10L);
		when(logUserPasswordHistoryMapper.selectDetailById(1L)).thenReturn(po);

		LogUserPasswordHistoryDetailVO detail = logUserPasswordHistoryService.getDetail(1L);

		assertThat(detail.getId()).isEqualTo(1L);
		assertThat(detail.getUserId()).isEqualTo(10L);
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class));
	}

	@Test
	@DisplayName("详情查询：记录不存在时抛出业务异常")
	void getDetailThrowsWhenNotFound() {
		when(logUserPasswordHistoryMapper.selectDetailById(99L)).thenReturn(null);

		assertThatThrownBy(() -> logUserPasswordHistoryService.getDetail(99L))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("分页查询：调用审计用户展示填充")
	void getPageInvokesAuditEnrichment() {
		Page<LogUserPasswordHistoryEntity> pageParams = new Page<>(1, 10);
		LogUserPasswordHistoryQuery query = new LogUserPasswordHistoryQuery();
		Page<com.auth.service.system.log.model.po.LogUserPasswordHistoryPageRowPO> emptyPage = new Page<>(1, 10);
		when(logUserPasswordHistoryMapper.selectListByPage(pageParams, query)).thenReturn(emptyPage);

		logUserPasswordHistoryService.getPage(pageParams, query);

		verify(auditUserDisplayService).enrichAuditUsernames(any(com.baomidou.mybatisplus.core.metadata.IPage.class));
	}

}
