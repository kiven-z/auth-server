package com.auth.service.system.schedule.model.enums;

import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.auth.service.system.common.exception.code.SystemScheduleResultCode.JOB_TASK_TYPE_UNSUPPORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SysJobTaskType} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobTaskType 任务类型解析")
class SysJobTaskTypeTest {

	@Test
	@DisplayName("显式传入 taskType 时优先使用 taskType")
	void resolveReturnsExplicitTaskType() {
		// 显式任务类型应直接解析。
		SysJobTaskType result = SysJobTaskType.resolve("CUSTOM_CLASS");

		assertThat(result).isEqualTo(SysJobTaskType.CUSTOM_CLASS);
	}

	@Test
	@DisplayName("显式传入 MESSAGE_DISPATCH 时直接返回消息任务类型")
	void resolveReturnsMessageDispatchTaskType() {
		SysJobTaskType result = SysJobTaskType.resolve("MESSAGE_DISPATCH");

		assertThat(result).isEqualTo(SysJobTaskType.MESSAGE_DISPATCH);
	}

	@Test
	@DisplayName("taskType 非法时抛出 JOB_TASK_TYPE_UNSUPPORTED")
	void resolveThrowsWhenTaskTypeInvalid() {
		assertThatThrownBy(() -> SysJobTaskType.resolve("INVALID_TYPE")).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_TASK_TYPE_UNSUPPORTED);
	}

}
