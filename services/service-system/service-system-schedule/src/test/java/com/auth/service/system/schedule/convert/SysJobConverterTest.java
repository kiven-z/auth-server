package com.auth.service.system.schedule.convert;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.po.SysJobDetailRowPO;
import com.auth.service.system.schedule.model.po.SysJobPageRowPO;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysJobConverter} 映射校验
 */
class SysJobConverterTest {

	@Test
	@DisplayName("分页 PO 映射为分页 VO，最近执行结果由 Service 补充")
	void toPageResponse_mapsPoFields() {
		LocalDateTime now = LocalDateTime.of(2026, 5, 23, 10, 0);
		SysJobPageRowPO po = new SysJobPageRowPO();
		po.setId(1L);
		po.setJobName("demo");
		po.setJobGroup("DEFAULT");
		po.setCronExpression("0 0/5 * * * ?");
		po.setStatus(true);
		po.setCreateTime(now);
		po.setCreateUser(100L);

		SysJobPageResponse vo = SysJobConverter.INSTANCE.toPageResponse(po);

		assertThat(vo.getId()).isEqualTo(1L);
		assertThat(vo.getJobName()).isEqualTo("demo");
		assertThat(vo.getCronExpression()).isEqualTo("0 0/5 * * * ?");
		assertThat(vo.getCreateTime()).isEqualTo(now);
		assertThat(vo.getCreateUsername()).isNull();
		assertThat(vo.getLastExecutionStatus()).isNull();
	}

	@Test
	@DisplayName("详情 PO 映射为详情 VO，运行时字段保持 null")
	void toDetailVo_mapsDetailRowFields() {
		SysJobDetailRowPO po = new SysJobDetailRowPO();
		po.setId(2L);
		po.setJobName("job");
		po.setJobGroup("DEFAULT");
		po.setJobGroupName("默认分组");
		po.setTaskType("BEAN_INVOKE");

		SysJobDetailVO vo = SysJobConverter.INSTANCE.toDetailVo(po);

		assertThat(vo.getId()).isEqualTo(2L);
		assertThat(vo.getTaskType()).isEqualTo("BEAN_INVOKE");
		assertThat(vo.getJobGroupName()).isEqualTo("默认分组");
		assertThat(vo.getNextFireTime()).isNull();
	}

	@Test
	@DisplayName("新增表单映射为实体，审计列不写入")
	void toEntity_mapsCreateForm() {
		SysJobCreateForm form = new SysJobCreateForm();
		form.setJobName("newJob");
		form.setJobGroup("G1");
		form.setCronExpression("0 0 12 * * ?");
		form.setMisfirePolicy(1);
		form.setConcurrent(false);
		form.setStatus(true);
		form.setTaskType("BEAN_INVOKE");

		SysJobEntity entity = SysJobConverter.INSTANCE.toEntity(form);

		assertThat(entity.getJobName()).isEqualTo("newJob");
		assertThat(entity.getId()).isNull();
		assertThat(entity.getCreateTime()).isNull();
	}

	@Test
	@DisplayName("更新表单 invokeTarget 为 null 时不覆盖实体原值")
	void updateEntity_skipsNullInvokeTarget() {
		SysJobUpdateForm form = new SysJobUpdateForm();
		form.setCronExpression("0 0 1 * * ?");
		form.setMisfirePolicy(2);
		form.setConcurrent(true);
		form.setStatus(false);
		form.setTaskType("BEAN_INVOKE");
		form.setHandlerCode("h1");
		form.setInvokeTarget(null);

		SysJobEntity entity = new SysJobEntity();
		entity.setInvokeTarget("old.target");
		entity.setCronExpression("0 0 12 * * ?");

		SysJobConverter.INSTANCE.updateEntity(form, entity);

		assertThat(entity.getInvokeTarget()).isEqualTo("old.target");
		assertThat(entity.getCronExpression()).isEqualTo("0 0 1 * * ?");
		assertThat(entity.getJobName()).isNull();
	}

	@Test
	@DisplayName("更新表单 startTime/endTime 为 null 时清空实体原值")
	void updateEntity_clearsScheduleWindowWhenNull() {
		LocalDateTime oldStart = LocalDateTime.of(2026, 6, 1, 8, 0);
		LocalDateTime oldEnd = LocalDateTime.of(2026, 12, 31, 18, 0);
		SysJobUpdateForm form = new SysJobUpdateForm();
		form.setCronExpression("0 0 1 * * ?");
		form.setMisfirePolicy(2);
		form.setConcurrent(true);
		form.setStatus(true);
		form.setTaskType("BEAN_INVOKE");
		form.setStartTime(null);
		form.setEndTime(null);

		SysJobEntity entity = new SysJobEntity();
		entity.setStartTime(oldStart);
		entity.setEndTime(oldEnd);

		SysJobConverter.INSTANCE.updateEntity(form, entity);

		assertThat(entity.getStartTime()).isNull();
		assertThat(entity.getEndTime()).isNull();
	}

	@Test
	@DisplayName("更新表单 invokeTarget 非 null 时覆盖实体")
	void updateEntity_overwritesInvokeTargetWhenPresent() {
		SysJobUpdateForm form = new SysJobUpdateForm();
		form.setCronExpression("0 0 1 * * ?");
		form.setMisfirePolicy(2);
		form.setConcurrent(true);
		form.setStatus(true);
		form.setTaskType("BEAN_INVOKE");
		form.setInvokeTarget("new.target");

		SysJobEntity entity = new SysJobEntity();
		entity.setInvokeTarget("old.target");
		entity.setJobName("fixedName");

		SysJobConverter.INSTANCE.updateEntity(form, entity);

		assertThat(entity.getInvokeTarget()).isEqualTo("new.target");
		assertThat(entity.getJobName()).isEqualTo("fixedName");
	}

}
