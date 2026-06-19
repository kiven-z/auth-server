package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.mapper.log.LogAuthorizationAuditMapper;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.po.logauthorizationaudit.LogAuthorizationAuditDetailRowPO;
import com.auth.service.system.admin.model.po.logauthorizationaudit.LogAuthorizationAuditPageRowPO;
import com.auth.service.system.admin.model.query.log.LogAuthorizationAuditQuery;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditDetailVO;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditPageVO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.service.AuditUserDisplayService;
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

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LogAuthorizationAuditQueryServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("LogAuthorizationAuditQueryServiceImpl 权限决策审计日志")
@ExtendWith(MockitoExtension.class)
class LogAuthorizationAuditQueryServiceImplTest {

	@Mock
	private LogAuthorizationAuditMapper logAuthorizationAuditMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private LogAuthorizationAuditQueryServiceImpl logAuthorizationAuditQueryService;

	@BeforeEach
	void setUp() throws Exception {
		logAuthorizationAuditQueryService = new LogAuthorizationAuditQueryServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(logAuthorizationAuditQueryService, logAuthorizationAuditMapper);
	}

	@Test
	@DisplayName("详情查询：存在记录时填充审计用户名")
	void getDetailEnrichesAuditUsernames() {
		LogAuthorizationAuditDetailRowPO po = new LogAuthorizationAuditDetailRowPO();
		po.setId(1L);
		po.setEventType("DENIED");
		po.setDecisionReason("missing permission");
		po.setCreateUser(10L);
		when(logAuthorizationAuditMapper.selectDetailById(1L)).thenReturn(po);

		LogAuthorizationAuditDetailVO detail = logAuthorizationAuditQueryService.getDetail(1L);

		assertThat(detail.getId()).isEqualTo(1L);
		assertThat(detail.getEventType()).isEqualTo("DENIED");
		assertThat(detail.getDecisionReason()).isEqualTo("missing permission");
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class));
	}

	@Test
	@DisplayName("详情查询：记录不存在时抛出业务异常")
	void getDetailThrowsWhenNotFound() {
		when(logAuthorizationAuditMapper.selectDetailById(99L)).thenReturn(null);

		assertThatThrownBy(() -> logAuthorizationAuditQueryService.getDetail(99L))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("分页查询：调用审计用户展示填充")
	void getPageInvokesAuditEnrichment() {
		Page<LogAuthorizationAuditEntity> pageParams = new Page<>(1, 10);
		LogAuthorizationAuditQuery query = new LogAuthorizationAuditQuery();
		Page<LogAuthorizationAuditPageRowPO> emptyPage = new Page<>(1, 10);
		when(logAuthorizationAuditMapper.selectListByPage(pageParams, query)).thenReturn(emptyPage);

		PageResponse<LogAuthorizationAuditPageVO> result = logAuthorizationAuditQueryService.getPage(pageParams, query);

		assertThat(result.getList()).isEmpty();
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class));
	}

}
