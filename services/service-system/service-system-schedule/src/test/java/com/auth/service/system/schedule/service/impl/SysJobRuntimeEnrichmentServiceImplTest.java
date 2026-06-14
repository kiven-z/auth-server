package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobLastExecutionStatus;
import com.auth.service.system.schedule.model.enums.SysJobQuartzRuntimeStatus;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.support.SysJobQuartzRuntimeSnapshot;
import com.auth.service.system.schedule.support.quartz.SysJobQuartzRuntimeReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobRuntimeEnrichmentServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobRuntimeEnrichmentServiceImpl 运行时信息补充")
@ExtendWith(MockitoExtension.class)
class SysJobRuntimeEnrichmentServiceImplTest {

	@Mock
	private SysJobQuartzRuntimeReader sysJobQuartzRuntimeReader;

	@Mock
	private LogJobMapper logJobMapper;

	@InjectMocks
	private SysJobRuntimeEnrichmentServiceImpl sysJobRuntimeEnrichmentService;

	@Test
	@DisplayName("最近执行补充：无日志时最近结果为 UNKNOWN")
	void enrichLastExecutionMarksUnknownWhenNoLog() {
		SysJobPageResponse row = pageRow();
		row.setId(1L);
		when(logJobMapper.selectLatestByJobIds(any())).thenReturn(List.of());

		sysJobRuntimeEnrichmentService.enrichLastExecution(List.of(row));

		assertThat(row.getLastExecutionStatus()).isEqualTo(SysJobLastExecutionStatus.UNKNOWN);
		assertThat(row.getLastExecutionTime()).isNull();
	}

	@Test
	@DisplayName("最近执行补充：最近日志失败时写入 FAILED 与时间")
	void enrichLastExecutionMarksFailedFromLog() {
		SysJobPageResponse row = pageRow();
		row.setId(1L);
		LogJobEntity failedLog = new LogJobEntity();
		failedLog.setJobId(1L);
		failedLog.setStatus(false);
		failedLog.setCreateTime(LocalDateTime.of(2026, 6, 13, 9, 0));
		when(logJobMapper.selectLatestByJobIds(any())).thenReturn(List.of(failedLog));

		sysJobRuntimeEnrichmentService.enrichLastExecution(List.of(row));

		assertThat(row.getLastExecutionStatus()).isEqualTo(SysJobLastExecutionStatus.FAILED);
		assertThat(row.getLastExecutionTime()).isEqualTo(failedLog.getCreateTime());
	}

	@Test
	@DisplayName("详情补充：写入 Quartz 运行态、触发时间与最近执行结果")
	void enrichDetailSetsRuntimeFireTimesAndLastExecution() {
		SysJobDetailVO detail = new SysJobDetailVO();
		detail.setId(1L);
		Date previous = new Date(1_700_000_000_000L);
		Date next = new Date(1_700_000_600_000L);
		when(sysJobQuartzRuntimeReader.getFireTimes("demo", "G"))
			.thenReturn(new SysJobQuartzRuntimeReader.FireTimes(previous, next));
		LocalDateTime fireTime = LocalDateTime.of(2026, 6, 13, 10, 0);
		when(sysJobQuartzRuntimeReader.resolveRuntimeSnapshot("demo", "G"))
			.thenReturn(SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.RUNNING, fireTime));
		LogJobEntity successLog = new LogJobEntity();
		successLog.setJobId(1L);
		successLog.setStatus(true);
		successLog.setCreateTime(LocalDateTime.of(2026, 6, 13, 9, 30));
		when(logJobMapper.selectLatestByJobIds(any())).thenReturn(List.of(successLog));

		sysJobRuntimeEnrichmentService.enrichDetail(detail, "demo", "G");

		assertThat(detail.getPreviousFireTime()).isNotNull();
		assertThat(detail.getNextFireTime()).isNotNull();
		assertThat(detail.getQuartzRuntimeStatus()).isEqualTo(SysJobQuartzRuntimeStatus.RUNNING);
		assertThat(detail.getQuartzFireTime()).isEqualTo(fireTime);
		assertThat(detail.getLastExecutionStatus()).isEqualTo(SysJobLastExecutionStatus.SUCCESS);
		assertThat(detail.getLastExecutionTime()).isEqualTo(successLog.getCreateTime());
	}

	@Test
	@DisplayName("busy 判定：RUNNING 或 PENDING 返回 true")
	void isBusyReturnsTrueForRunningOrPending() {
		when(sysJobQuartzRuntimeReader.resolveRuntimeSnapshot("demo", "G"))
			.thenReturn(SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.RUNNING, null));

		assertThat(sysJobRuntimeEnrichmentService.isBusy("demo", "G")).isTrue();

		when(sysJobQuartzRuntimeReader.resolveRuntimeSnapshot("demo", "G"))
			.thenReturn(SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.PENDING, null));
		assertThat(sysJobRuntimeEnrichmentService.isBusy("demo", "G")).isTrue();

		when(sysJobQuartzRuntimeReader.resolveRuntimeSnapshot("demo", "G"))
			.thenReturn(SysJobQuartzRuntimeSnapshot.of(SysJobQuartzRuntimeStatus.IDLE, null));
		assertThat(sysJobRuntimeEnrichmentService.isBusy("demo", "G")).isFalse();
	}

	private SysJobPageResponse pageRow() {
		SysJobPageResponse row = new SysJobPageResponse();
		row.setJobName("demo");
		row.setJobGroup("G");
		return row;
	}

}
