package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.mapper.SysJobGroupMapper;
import com.auth.service.system.schedule.mapper.SysJobMapper;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.entity.SysJobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobStatusForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.po.SysJobDetailRowPO;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.service.SysJobRuntimeEnrichmentService;
import com.auth.service.system.schedule.support.SysJobSchedulerManager;
import com.auth.service.system.schedule.validation.SysJobDefinitionValidationManager;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.quartz.SchedulerException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_BUSY;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_GROUP_NOT_AVAILABLE;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_QUARTZ_OPERATION_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

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
	private LogJobMapper logJobMapper;

	@Mock
	private SysJobDefinitionValidationManager validationManager;

	@Mock
	private SysJobRuntimeEnrichmentService sysJobRuntimeEnrichmentService;

	private SysJobDefinitionServiceImpl sysJobDefinitionService;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SysJobEntity.class);
	}

	@BeforeEach
	void setUp() throws Exception {
		sysJobDefinitionService = spy(new SysJobDefinitionServiceImpl(quartzAdmin, auditUserDisplayService,
				sysJobGroupMapper, logJobMapper, validationManager, sysJobRuntimeEnrichmentService));
		Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysJobDefinitionService, sysJobMapper);
		lenient().doReturn(true).when(sysJobDefinitionService).updateBatchById(anyList());
	}

	private static SysJobCreateForm minimalCreateForm(String jobGroup, boolean status) {
		SysJobCreateForm form = new SysJobCreateForm();
		form.setJobName("demoJob");
		form.setJobGroup(jobGroup);
		form.setTaskType("BEAN_INVOKE");
		form.setInvokeTarget("demoTask.run()");
		form.setCronExpression("0 0 12 * * ?");
		form.setMisfirePolicy(2);
		form.setConcurrent(false);
		form.setStatus(status);
		return form;
	}

	private static SysJobGroupEntity enabledGroup(String groupCode) {
		SysJobGroupEntity group = new SysJobGroupEntity();
		group.setGroupCode(groupCode);
		group.setStatus(true);
		return group;
	}

	@Test
	@DisplayName("新增：分组不存在时抛出 DATA_NOT_EXIST")
	void createThrowsWhenJobGroupMissing() {
		when(sysJobGroupMapper.selectOne(any())).thenReturn(null);
		SysJobCreateForm form = minimalCreateForm("MISSING", true);

		assertThatThrownBy(() -> sysJobDefinitionService.create(form)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
		verify(sysJobMapper, never()).insert(org.mockito.ArgumentMatchers.<SysJobEntity>any());
	}

	@Test
	@DisplayName("新增：分组已禁用时抛出 JOB_GROUP_NOT_AVAILABLE")
	void createThrowsWhenJobGroupDisabled() {
		SysJobGroupEntity disabledGroup = enabledGroup("DISABLED");
		disabledGroup.setStatus(false);
		when(sysJobGroupMapper.selectOne(any())).thenReturn(disabledGroup);
		SysJobCreateForm form = minimalCreateForm("DISABLED", true);

		assertThatThrownBy(() -> sysJobDefinitionService.create(form)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_GROUP_NOT_AVAILABLE);
		verify(sysJobMapper, never()).insert(org.mockito.ArgumentMatchers.<SysJobEntity>any());
	}

	@Test
	@DisplayName("新增：启用任务落库并注册 Quartz")
	void createEnabledJobPersistsAndRegistersQuartz() throws SchedulerException {
		when(sysJobGroupMapper.selectOne(any())).thenReturn(enabledGroup("DEFAULT"));
		when(sysJobMapper.insert(any(SysJobEntity.class))).thenReturn(1);

		sysJobDefinitionService.create(minimalCreateForm("DEFAULT", true));

		ArgumentCaptor<SysJobEntity> entityCaptor = ArgumentCaptor.forClass(SysJobEntity.class);
		verify(sysJobMapper).insert(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getJobName()).isEqualTo("demoJob");
		assertThat(entityCaptor.getValue().getStatus()).isTrue();
		verify(quartzAdmin).scheduleOrReplace(any(SysJobEntity.class));
	}

	@Test
	@DisplayName("新增：暂停任务仅落库，不注册 Quartz")
	void createPausedJobPersistsWithoutQuartzRegister() {
		when(sysJobGroupMapper.selectOne(any())).thenReturn(enabledGroup("DEFAULT"));
		when(sysJobMapper.insert(any(SysJobEntity.class))).thenReturn(1);

		sysJobDefinitionService.create(minimalCreateForm("DEFAULT", false));

		ArgumentCaptor<SysJobEntity> entityCaptor = ArgumentCaptor.forClass(SysJobEntity.class);
		verify(sysJobMapper).insert(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getStatus()).isFalse();
		verifyNoInteractions(quartzAdmin);
	}

	@Test
	@DisplayName("详情：存在时补全分组名称与运行时信息")
	void getDetailReturnsVoWhenExists() {
		SysJobDetailRowPO detailRow = new SysJobDetailRowPO();
		detailRow.setId(1L);
		detailRow.setJobName("demoJob");
		detailRow.setJobGroup("DEFAULT");
		detailRow.setJobGroupName("默认分组");
		detailRow.setTaskType("BEAN_INVOKE");
		when(sysJobMapper.selectDetailById(1L)).thenReturn(detailRow);

		SysJobDetailVO detail = sysJobDefinitionService.getDetail(1L);

		assertThat(detail.getJobName()).isEqualTo("demoJob");
		assertThat(detail.getJobGroup()).isEqualTo("DEFAULT");
		assertThat(detail.getJobGroupName()).isEqualTo("默认分组");
		verify(sysJobRuntimeEnrichmentService).enrichDetail(detail, "demoJob", "DEFAULT");
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class));
	}

	@Test
	@DisplayName("详情：不存在时抛出 DATA_NOT_EXIST")
	void getDetailThrowsWhenMissing() {
		when(sysJobMapper.selectDetailById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysJobDefinitionService.getDetail(99L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
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
	@DisplayName("立即执行：任务不存在时抛出 DATA_NOT_EXIST")
	void runOnceThrowsWhenMissing() {
		when(sysJobMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysJobDefinitionService.runOnce(99L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("立即执行：存在时 ensureJobDetail 并 trigger")
	void runOnceTriggersWhenExists() throws SchedulerException {
		SysJobEntity entity = new SysJobEntity();
		entity.setId(5L);
		entity.setJobName("demoJob");
		entity.setJobGroup("DEFAULT");
		when(sysJobMapper.selectById(5L)).thenReturn(entity);
		when(sysJobRuntimeEnrichmentService.isBusy("demoJob", "DEFAULT")).thenReturn(false);

		sysJobDefinitionService.runOnce(5L);

		verify(quartzAdmin).ensureJobDetail(entity);
		verify(quartzAdmin).triggerJob("demoJob", "DEFAULT");
	}

	@Test
	@DisplayName("立即执行：任务 busy 时抛出 JOB_BUSY")
	void runOnceThrowsWhenBusy() {
		SysJobEntity entity = new SysJobEntity();
		entity.setId(5L);
		entity.setJobName("demoJob");
		entity.setJobGroup("DEFAULT");
		when(sysJobMapper.selectById(5L)).thenReturn(entity);
		when(sysJobRuntimeEnrichmentService.isBusy("demoJob", "DEFAULT")).thenReturn(true);

		assertThatThrownBy(() -> sysJobDefinitionService.runOnce(5L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_BUSY);
	}

	@Test
	@DisplayName("批量停用：跳过不存在任务，批量落库并 pauseJob")
	void batchUpdateStatusSkipsMissingAndPausesQuartz() throws SchedulerException {
		SysJobEntity entity = new SysJobEntity();
		entity.setId(1L);
		entity.setJobName("demoJob");
		entity.setJobGroup("DEFAULT");
		entity.setStatus(true);
		when(sysJobMapper.selectList(any())).thenReturn(List.of(entity));

		SysJobStatusForm form = new SysJobStatusForm();
		form.setIds(List.of(1L, 2L));
		form.setStatus(false);

		sysJobDefinitionService.batchUpdateStatus(form);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<SysJobEntity>> updateCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(sysJobDefinitionService).updateBatchById(updateCaptor.capture());
		assertThat(updateCaptor.getValue()).hasSize(2).extracting(SysJobEntity::getId).containsExactly(1L, 2L);
		assertThat(updateCaptor.getValue()).allMatch(job -> BooleanUtil.isFalse(job.getStatus()));
		verify(quartzAdmin).pauseJob("demoJob", "DEFAULT");
		verify(quartzAdmin, never()).scheduleOrReplace(any());
	}

	@Test
	@DisplayName("批量启用：仅对原暂停任务注册 Quartz")
	void batchUpdateStatusEnableOnlySchedulesPreviouslyPausedJobs() throws SchedulerException {
		SysJobEntity paused = new SysJobEntity();
		paused.setId(2L);
		paused.setJobName("jobB");
		paused.setJobGroup("DEFAULT");
		paused.setStatus(false);
		when(sysJobMapper.selectList(any())).thenReturn(List.of(paused));

		SysJobStatusForm form = new SysJobStatusForm();
		form.setIds(List.of(1L, 2L));
		form.setStatus(true);

		sysJobDefinitionService.batchUpdateStatus(form);

		verify(sysJobDefinitionService).updateBatchById(anyList());
		verify(quartzAdmin, times(1)).scheduleOrReplace(any());
		verify(quartzAdmin)
			.scheduleOrReplace(argThat(job -> job.getId().equals(2L) && BooleanUtil.isTrue(job.getStatus())));
	}

	@Test
	@DisplayName("批量启停：Quartz 同步失败时抛出 JOB_QUARTZ_OPERATION_FAILED")
	void batchUpdateStatusThrowsWhenQuartzSyncFails() throws SchedulerException {
		SysJobEntity entity = new SysJobEntity();
		entity.setId(1L);
		entity.setJobName("demoJob");
		entity.setJobGroup("DEFAULT");
		entity.setStatus(true);
		when(sysJobMapper.selectList(any())).thenReturn(List.of(entity));
		doThrow(new SchedulerException("sync failed")).when(quartzAdmin).pauseJob("demoJob", "DEFAULT");

		SysJobStatusForm form = new SysJobStatusForm();
		form.setIds(List.of(1L));
		form.setStatus(false);

		assertThatThrownBy(() -> sysJobDefinitionService.batchUpdateStatus(form)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_QUARTZ_OPERATION_FAILED);
	}

}
