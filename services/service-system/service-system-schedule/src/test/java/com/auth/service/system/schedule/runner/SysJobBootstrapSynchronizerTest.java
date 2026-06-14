package com.auth.service.system.schedule.runner;

import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobBootstrapSynchronizer} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobBootstrapSynchronizer 启动同步")
@ExtendWith(MockitoExtension.class)
class SysJobBootstrapSynchronizerTest {

	@Mock
	private SysJobMapper sysJobMapper;

	@Mock
	private SysJobSchedulerManager sysJobSchedulerManager;

	private ScheduleJobProperties scheduleJobProperties;

	private SysJobBootstrapSynchronizer sysJobBootstrapSynchronizer;

	private ApplicationReadyEvent applicationReadyEvent;

	@BeforeEach
	void setUp() {
		scheduleJobProperties = new ScheduleJobProperties();
		scheduleJobProperties.setBootstrapBatchSize(100);
		sysJobBootstrapSynchronizer = new SysJobBootstrapSynchronizer(sysJobMapper, sysJobSchedulerManager,
				scheduleJobProperties);
		ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
		applicationReadyEvent = new ApplicationReadyEvent(mock(SpringApplication.class), new String[] {}, context,
				Duration.ZERO);
	}

	@Test
	@DisplayName("应用就绪后应同步 Quartz 任务")
	void onApplicationReady_schedulesEnabledAndPausesDisabled() throws Exception {
		SysJobEntity enabledJob = new SysJobEntity();
		enabledJob.setId(1L);
		enabledJob.setJobName("enabledJob");
		enabledJob.setJobGroup("DEFAULT");
		enabledJob.setStatus(true);

		SysJobEntity disabledJob = new SysJobEntity();
		disabledJob.setId(2L);
		disabledJob.setJobName("disabledJob");
		disabledJob.setJobGroup("DEFAULT");
		disabledJob.setStatus(false);

		Page<SysJobEntity> enabledPage = new Page<>(1, 100);
		enabledPage.setRecords(List.of(enabledJob));
		enabledPage.setTotal(1);
		enabledPage.setPages(1);

		Page<SysJobEntity> disabledPage = new Page<>(1, 100);
		disabledPage.setRecords(List.of(disabledJob));
		disabledPage.setTotal(1);
		disabledPage.setPages(1);

		when(sysJobMapper.selectPage(any(Page.class), any())).thenReturn(enabledPage, disabledPage);

		sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent);

		verify(sysJobSchedulerManager).scheduleOrReplace(enabledJob);
		verify(sysJobSchedulerManager).pauseJob("disabledJob", "DEFAULT");
	}

	@Test
	@DisplayName("同步：按配置分页大小查询")
	void onApplicationReady_usesConfiguredBatchSize() {
		scheduleJobProperties.setBootstrapBatchSize(50);
		sysJobBootstrapSynchronizer = new SysJobBootstrapSynchronizer(sysJobMapper, sysJobSchedulerManager,
				scheduleJobProperties);

		Page<SysJobEntity> emptyPage = new Page<>(1, 50);
		emptyPage.setRecords(List.of());
		emptyPage.setTotal(0);
		emptyPage.setPages(0);
		when(sysJobMapper.selectPage(any(Page.class), any())).thenReturn(emptyPage);

		sysJobBootstrapSynchronizer.onApplicationReady(applicationReadyEvent);

		ArgumentCaptor<Page<SysJobEntity>> pageCaptor = ArgumentCaptor.forClass(Page.class);
		verify(sysJobMapper, times(2)).selectPage(pageCaptor.capture(), any());
		assertThat(pageCaptor.getAllValues()).allMatch(page -> page.getSize() == 50);
	}

}
