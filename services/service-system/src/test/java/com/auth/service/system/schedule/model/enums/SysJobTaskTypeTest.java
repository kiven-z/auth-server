package com.auth.service.system.schedule.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
		// 显式任务类型应覆盖历史推断逻辑。
		SysJobTaskType result = SysJobTaskType.resolve("CUSTOM_CLASS", "demoBean.run()");

		assertThat(result).isEqualTo(SysJobTaskType.CUSTOM_CLASS);
	}

	@Test
	@DisplayName("显式传入 MESSAGE_DISPATCH 时直接返回消息任务类型")
	void resolveReturnsMessageDispatchTaskType() {
		// 消息派发类型必须可直接解析，避免被 invokeTarget 推断覆盖。
		SysJobTaskType result = SysJobTaskType.resolve("MESSAGE_DISPATCH", null);

		assertThat(result).isEqualTo(SysJobTaskType.MESSAGE_DISPATCH);
	}

	@Test
	@DisplayName("taskType 为空时按 invokeTarget 推断为 BEAN_INVOKE")
	void resolveInfersBeanInvokeWhenInvokeTargetPresent() {
		// 历史数据兼容：invokeTarget 非空视为 Bean 调用模式。
		SysJobTaskType result = SysJobTaskType.resolve(null, "demoBean.run()");

		assertThat(result).isEqualTo(SysJobTaskType.BEAN_INVOKE);
	}

	@Test
	@DisplayName("taskType 为空时按 invokeTarget 推断为 CUSTOM_CLASS")
	void resolveInfersCustomClassWhenInvokeTargetBlank() {
		// 历史数据兼容：invokeTarget 为空视为自定义类模式。
		SysJobTaskType result = SysJobTaskType.resolve(null, " ");

		assertThat(result).isEqualTo(SysJobTaskType.CUSTOM_CLASS);
	}

}
