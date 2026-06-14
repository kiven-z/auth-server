package com.auth.service.system.schedule.convert;

import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.po.LogJobPageRowPO;
import com.auth.service.system.schedule.model.vo.LogJobDetailVO;
import com.auth.service.system.schedule.model.vo.LogJobPageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LogJobConverter} 映射校验（1=成功，0=失败）
 */
@DisplayName("LogJobConverter 任务日志映射")
class LogJobConverterTest {

	@Test
	@DisplayName("分页 PO status=1 映射为 true（成功）")
	void toPageResponse_mapsSuccessStatus() {
		LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
		LogJobPageRowPO po = new LogJobPageRowPO();
		po.setId(10L);
		po.setJobName("demoJob");
		po.setJobGroup("DEFAULT");
		po.setInvokeTarget("com.example.Task.run");
		po.setJobMessage("ok");
		po.setStatus(true);
		po.setElapsedTime(100L);
		po.setCreateTime(now);
		po.setUpdateTime(now);
		po.setCreateUser(1L);
		po.setUpdateUser(2L);

		LogJobPageResponse vo = LogJobConverter.INSTANCE.toPageResponse(po);

		assertThat(vo.getId()).isEqualTo(10L);
		assertThat(vo.getJobName()).isEqualTo("demoJob");
		assertThat(vo.getStatus()).isTrue();
		assertThat(vo.getCreateTime()).isEqualTo(now);
		assertThat(vo.getCreateUsername()).isNull();
	}

	@Test
	@DisplayName("分页 PO status=0 映射为 false（失败）")
	void toPageResponse_mapsFailureStatus() {
		LogJobPageRowPO po = new LogJobPageRowPO();
		po.setStatus(false);

		LogJobPageResponse vo = LogJobConverter.INSTANCE.toPageResponse(po);

		assertThat(vo.getStatus()).isFalse();
	}

	@Test
	@DisplayName("实体 status=true 映射为 API true（成功）")
	void toDetailVo_mapsEntitySuccess() {
		LogJobEntity entity = new LogJobEntity();
		entity.setId(1L);
		entity.setJobName("demo");
		entity.setStatus(Boolean.TRUE);

		LogJobDetailVO vo = LogJobConverter.INSTANCE.toDetailVo(entity);

		assertThat(vo.getStatus()).isTrue();
	}

	@Test
	@DisplayName("实体 status=false 映射为 API false（失败）")
	void toDetailVo_mapsEntityFailure() {
		LogJobEntity entity = new LogJobEntity();
		entity.setStatus(Boolean.FALSE);

		LogJobDetailVO vo = LogJobConverter.INSTANCE.toDetailVo(entity);

		assertThat(vo.getStatus()).isFalse();
	}

}
