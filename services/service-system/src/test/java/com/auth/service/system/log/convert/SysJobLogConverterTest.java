package com.auth.service.system.log.convert;

import com.auth.service.system.log.model.po.SysJobLogPageRowPO;
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
		assertThat(vo.getStatus()).isZero();
		assertThat(vo.getCreateTime()).isEqualTo(now);
		assertThat(vo.getUpdateTime()).isEqualTo(now);
		assertThat(vo.getCreateUser()).isEqualTo(1L);
		assertThat(vo.getUpdateUser()).isEqualTo(2L);
		assertThat(vo.getCreateUsername()).isNull();
		assertThat(vo.getUpdateUsername()).isNull();
	}

}
