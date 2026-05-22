package com.auth.service.system.log.convert;

import com.auth.service.system.log.model.entity.JobLogEntity;
import com.auth.service.system.log.model.po.SysJobLogPageRowPO;
import com.auth.service.system.log.model.vo.LogJobDetailVO;
import com.auth.service.system.log.model.vo.LogJobPageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysJobLogConverter} 映射校验
 */
class SysJobLogConverterTest {

	@Test
	@DisplayName("任务日志分页 PO 转 VO 时应映射审计字段且用户名由后续 enrich 填充")
	void toPageResponse_mapsAuditFields() {
		LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
		SysJobLogPageRowPO po = new SysJobLogPageRowPO();
		po.setId(10L);
		po.setJobName("demoJob");
		po.setJobGroup("DEFAULT");
		po.setInvokeTarget("com.example.Task.run");
		po.setJobMessage("ok");
		po.setStatus(0);
		po.setElapsedTime(100L);
		po.setCreateTime(now);
		po.setUpdateTime(now);
		po.setCreateUser(1L);
		po.setUpdateUser(2L);

		LogJobPageResponse vo = SysJobLogConverter.INSTANCE.toPageResponse(po);

		// 方法内：校验业务字段与 BaseResponse 审计字段
		assertThat(vo.getId()).isEqualTo(10L);
		assertThat(vo.getJobName()).isEqualTo("demoJob");
		assertThat(vo.getStatus()).isTrue();
		assertThat(vo.getCreateTime()).isEqualTo(now);
		assertThat(vo.getUpdateTime()).isEqualTo(now);
		assertThat(vo.getCreateUser()).isEqualTo(1L);
		assertThat(vo.getUpdateUser()).isEqualTo(2L);
		assertThat(vo.getCreateUsername()).isNull();
		assertThat(vo.getUpdateUsername()).isNull();
	}

	@Test
	@DisplayName("分页 PO status=1 映射为 false（失败）")
	void toPageResponse_mapsFailureStatus() {
		SysJobLogPageRowPO po = new SysJobLogPageRowPO();
		po.setStatus(1);

		LogJobPageResponse vo = SysJobLogConverter.INSTANCE.toPageResponse(po);

		assertThat(vo.getStatus()).isFalse();
	}

	@Test
	@DisplayName("任务日志实体转详情 VO 时 status 映射为 true=成功")
	void toDetailVo_mapsStatusAsBooleanSuccess() {
		JobLogEntity entity = new JobLogEntity();
		entity.setId(1L);
		entity.setJobName("demo");
		entity.setStatus(Boolean.FALSE);

		LogJobDetailVO vo = SysJobLogConverter.INSTANCE.toDetailVo(entity);

		assertThat(vo.getStatus()).isTrue();
	}

	@Test
	@DisplayName("任务日志实体 status=true(失败) 映射为 API false")
	void toDetailVo_mapsStatusAsBooleanFailure() {
		JobLogEntity entity = new JobLogEntity();
		entity.setStatus(Boolean.TRUE);

		LogJobDetailVO vo = SysJobLogConverter.INSTANCE.toDetailVo(entity);

		assertThat(vo.getStatus()).isFalse();
	}

}
