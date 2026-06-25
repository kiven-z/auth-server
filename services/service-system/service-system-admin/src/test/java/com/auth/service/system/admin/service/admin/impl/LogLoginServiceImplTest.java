package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.system.admin.mapper.log.LogLoginLogMapper;
import com.auth.service.system.admin.model.po.loglogin.LogLoginLogPageRowPO;
import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogDetailVO;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
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

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LogLoginQueryServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("LogLoginQueryServiceImpl 登录日志")
@ExtendWith(MockitoExtension.class)
class LogLoginQueryServiceImplTest {

	@Mock
	private LogLoginLogMapper logLoginLogMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private LogLoginQueryServiceImpl logLoginQueryService;

	@BeforeEach
	void setUp() throws Exception {
		logLoginQueryService = new LogLoginQueryServiceImpl(auditUserDisplayService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(logLoginQueryService, logLoginLogMapper);
	}

	@Test
	@DisplayName("分页查询：调用审计用户展示填充")
	void getPageInvokesAuditEnrichment() {
		Page<LoginLogEntity> pageParams = new Page<>(1, 10);
		LogLoginLogQuery query = new LogLoginLogQuery();
		Page<LogLoginLogPageRowPO> emptyPage = new Page<>(1, 10);
		when(logLoginLogMapper.selectListByPage(pageParams, query)).thenReturn(emptyPage);

		PageResponse<LogLoginLogPageVO> result = logLoginQueryService.getPage(pageParams, query);

		assertThat(result.getList()).isEmpty();
		verify(auditUserDisplayService).enrichAuditUsernames(any(IPage.class), isNull(), isNull());
	}

	@Test
	@DisplayName("详情查询：存在记录时返回转换结果")
	void getDetailReturnsConvertedEntity() {
		LoginLogEntity entity = new LoginLogEntity();
		entity.setId(5L);
		entity.setUserId(100L);
		entity.setUsername("tester");
		entity.setLoginResult(1);
		when(logLoginLogMapper.selectById(5L)).thenReturn(entity);

		LogLoginLogDetailVO detail = logLoginQueryService.getDetail(5L);

		assertThat(detail.getId()).isEqualTo(5L);
		assertThat(detail.getUserId()).isEqualTo(100L);
		assertThat(detail.getUsername()).isEqualTo("tester");
		assertThat(detail.getLoginResult()).isEqualTo(1);
	}

	@Test
	@DisplayName("详情查询：记录不存在时抛出业务异常")
	void getDetailThrowsWhenNotFound() {
		when(logLoginLogMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> logLoginQueryService.getDetail(99L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

}
