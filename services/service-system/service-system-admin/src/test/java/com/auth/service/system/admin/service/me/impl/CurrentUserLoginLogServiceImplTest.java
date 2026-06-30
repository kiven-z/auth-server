package com.auth.service.system.admin.service.me.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.service.system.admin.model.query.log.LogLoginLogQuery;
import com.auth.service.system.admin.model.query.me.MeLoginLogPageQuery;
import com.auth.service.system.admin.model.vo.loglogin.LogLoginLogPageVO;
import com.auth.service.system.admin.model.vo.me.MeLoginLogPageVO;
import com.auth.service.system.admin.service.admin.LogLoginQueryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link CurrentUserLoginLogServiceImpl} 单元测试
 */
@DisplayName("CurrentUserLoginLogServiceImpl 个人中心登录日志")
@ExtendWith(MockitoExtension.class)
class CurrentUserLoginLogServiceImplTest {

	@Mock
	private LogLoginQueryService logLoginQueryService;

	@InjectMocks
	private CurrentUserLoginLogServiceImpl currentUserLoginLogService;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("分页查询：强制当前用户与180天窗口并返回精简字段")
	void getPageForcesUserAndWindowAndMapsSlimVo() {
		AuthProfile profile = AuthProfile.builder().userId(100L).username("tester").build();
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(profile, null));

		MeLoginLogPageQuery query = new MeLoginLogPageQuery();
		query.setPageIndex(1);
		query.setPageSize(10);
		query.setLoginType("PASSWORD");
		query.setLoginResult(1);

		LogLoginLogPageVO adminVo = new LogLoginLogPageVO();
		adminVo.setLoginTime(LocalDateTime.of(2026, 1, 1, 10, 0));
		adminVo.setLoginRegion("Shanghai");
		adminVo.setLoginResult(1);
		adminVo.setLoginType("PASSWORD");
		adminVo.setUserId(100L);
		adminVo.setUsername("tester");
		PageResponse<LogLoginLogPageVO> adminPage = PageResponse.of(1L, 10L, 1L, List.of(adminVo));
		when(logLoginQueryService.getPage(any(Page.class), any(LogLoginLogQuery.class))).thenReturn(adminPage);

		Page<LoginLogEntity> pageParams = new Page<>(1, 10);
		PageResponse<MeLoginLogPageVO> result = currentUserLoginLogService.getPage(pageParams, query);

		ArgumentCaptor<LogLoginLogQuery> queryCaptor = ArgumentCaptor.forClass(LogLoginLogQuery.class);
		verify(logLoginQueryService).getPage(any(Page.class), queryCaptor.capture());
		LogLoginLogQuery internalQuery = queryCaptor.getValue();
		assertThat(internalQuery.getUserId()).isEqualTo(100L);
		assertThat(internalQuery.getLoginType()).isEqualTo("PASSWORD");
		assertThat(internalQuery.getLoginResult()).isEqualTo(1);
		assertThat(internalQuery.getLoginTimeStart()).isNotNull();
		assertThat(internalQuery.getLoginTimeEnd()).isNotNull();
		assertThat(internalQuery.getLoginTimeStart()).isBeforeOrEqualTo(internalQuery.getLoginTimeEnd());

		assertThat(result.getList()).hasSize(1);
		MeLoginLogPageVO meVo = result.getList().get(0);
		assertThat(meVo.getLoginType()).isEqualTo("PASSWORD");
		assertThat(meVo.getLoginResult()).isEqualTo(1);
		assertThat(meVo.getLoginRegion()).isEqualTo("Shanghai");
	}

}
