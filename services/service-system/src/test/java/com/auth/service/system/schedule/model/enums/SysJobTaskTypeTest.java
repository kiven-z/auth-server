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
