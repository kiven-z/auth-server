package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.mapper.JobLogMapper;
import com.auth.service.system.schedule.mapper.SysJobGroupMapper;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.auth.service.system.schedule.validation.SysJobDefinitionValidationManager;
import com.auth.service.system.schedule.service.SysJobQuartzRuntimeQueryService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobDefinitionServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobDefinitionServiceImpl 定时任务定义")
@ExtendWith(MockitoExtension.class)
class SysJobDefinitionServiceImplTest {

	@Mock
	private SysJobMapper sysJobMapper;

	@Mock
	private SysJobSchedulerManager quartzAdmin;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private SysJobGroupMapper sysJobGroupMapper;

	@Mock
	private JobLogMapper jobLogMapper;

	@Mock
	private SysJobDefinitionValidationManager validationManager;

	@Mock
	private SysJobQuartzRuntimeQueryService sysJobQuartzRuntimeQueryService;

	private SysJobDefinitionServiceImpl sysJobDefinitionService;

	@BeforeEach
	void setUp() throws Exception {
		sysJobDefinitionService = new SysJobDefinitionServiceImpl(quartzAdmin, auditUserDisplayService,
				sysJobGroupMapper, jobLogMapper, validationManager, sysJobQuartzRuntimeQueryService);
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysJobDefinitionService, sysJobMapper);
	}

	@Test
	@DisplayName("详情：存在时补全分组名称、Quartz 触发时间与审计展示名")
	void getDetailReturnsVoWhenExists() {
		SysJobEntity entity = new SysJobEntity();
		entity.setId(1L);
		entity.setJobName("demoJob");
		entity.setJobGroup("DEFAULT");
		entity.setTaskType("BEAN_INVOKE");
		when(sysJobMapper.selectById(1L)).thenReturn(entity);

		SysJobGroupEntity groupEntity = new SysJobGroupEntity();
		groupEntity.setGroupCode("DEFAULT");
		groupEntity.setGroupName("默认分组");
		when(sysJobGroupMapper.selectOne(any(Wrapper.class))).thenReturn(groupEntity);
		when(quartzAdmin.getFireTimes("demoJob", "DEFAULT"))
			.thenReturn(new SysJobSchedulerManager.FireTimes(null, null));

		SysJobDetailVO detail = sysJobDefinitionService.getDetail(1L);

		assertThat(detail.getJobName()).isEqualTo("demoJob");
		assertThat(detail.getJobGroup()).isEqualTo("DEFAULT");
		assertThat(detail.getJobGroupName()).isEqualTo("默认分组");
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class));
	}

	@Test
	@DisplayName("更新：清空开始/结束时间后持久化实体对应字段为 null")
	void updateClearsScheduleWindowWhenFormTimesAreNull() throws Exception {
		LocalDateTime oldStart = LocalDateTime.of(2026, 6, 1, 8, 0);
		LocalDateTime oldEnd = LocalDateTime.of(2026, 12, 31, 18, 0);
		SysJobEntity dbJob = new SysJobEntity();
		dbJob.setId(10L);
		dbJob.setJobName("demoJob");
		dbJob.setJobGroup("DEFAULT");
		dbJob.setTaskType("BEAN_INVOKE");
		dbJob.setCronExpression("0 0 12 * * ?");
		dbJob.setMisfirePolicy(2);
		dbJob.setConcurrent(false);
		dbJob.setStatus(true);
		dbJob.setStartTime(oldStart);
		dbJob.setEndTime(oldEnd);
		when(sysJobMapper.selectById("10")).thenReturn(dbJob);
		doAnswer(invocation -> {
			SysJobEntity entity = invocation.getArgument(0);
			entity.setVersion(1L);
			return 1;
		}).when(sysJobMapper).updateById(any(SysJobEntity.class));

		SysJobUpdateForm form = new SysJobUpdateForm();
		form.setId("10");
		form.setCronExpression("0 0 1 * * ?");
		form.setMisfirePolicy(2);
		form.setConcurrent(true);
		form.setStatus(true);
		form.setTaskType("BEAN_INVOKE");
		form.setStartTime(null);
		form.setEndTime(null);

		sysJobDefinitionService.update(form);

		ArgumentCaptor<SysJobEntity> entityCaptor = ArgumentCaptor.forClass(SysJobEntity.class);
		verify(sysJobMapper).updateById(entityCaptor.capture());
		SysJobEntity persisted = entityCaptor.getValue();
		assertThat(persisted.getStartTime()).isNull();
		assertThat(persisted.getEndTime()).isNull();
		verify(quartzAdmin).scheduleOrReplace(persisted);
	}

	@Test
	@DisplayName("详情：任务不存在时抛出 DATA_NOT_EXIST")
	void getDetailThrowsWhenMissing() {
		when(sysJobMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysJobDefinitionService.getDetail(99L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

}
