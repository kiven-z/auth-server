package com.auth.service.system.schedule.model.enums;

import com.auth.service.system.schedule.task.SysJobEntryJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysJobTaskType} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysJobTaskType 任务类型枚举")
class SysJobTaskTypeTest {

	@Test
	@DisplayName("resolveEntryJobClass：所有类型共用统一入口 Job")
	void resolveEntryJobClassReturnsUnifiedEntryJobs() {
		for (SysJobTaskType taskType : SysJobTaskType.values()) {
			assertThat(taskType.resolveEntryJobClass(true)).isEqualTo(SysJobEntryJob.class);
			assertThat(taskType.resolveEntryJobClass(false)).isEqualTo(SysJobEntryJob.Disallow.class);
		}
	}

}
