package com.auth.service.system.schedule.handler;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.JOB_TASK_TYPE_UNSUPPORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SysJobTaskHandlerRegistry} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobTaskHandlerRegistry 任务处理器注册中心")
class SysJobTaskHandlerRegistryTest {

	@Test
	@DisplayName("resolve：根据任务类型返回对应处理器")
	void resolveReturnsMatchedHandler() {
		// 准备处理器注册中心。
		SysJobTaskHandlerRegistry registry = new SysJobTaskHandlerRegistry(
				List.of(new BeanInvokeSysJobTaskHandler(), new CustomClassSysJobTaskHandler()));
		SysJobEntity job = new SysJobEntity();
		job.setTaskType(SysJobTaskType.BEAN_INVOKE.name());
		job.setInvokeTarget("demoBean.run()");

		// 执行处理器解析并断言结果。
		SysJobTaskHandler handler = registry.resolve(job);
		assertThat(handler).isInstanceOf(BeanInvokeSysJobTaskHandler.class);
	}

	@Test
	@DisplayName("resolve：任务类型没有注册处理器时抛出业务异常")
	void resolveThrowsWhenTaskTypeNotRegistered() {
		// 准备仅包含 BEAN_INVOKE 的注册中心，制造未注册类型场景。
		SysJobTaskHandlerRegistry registry = new SysJobTaskHandlerRegistry(List.of(new BeanInvokeSysJobTaskHandler()));
		SysJobEntity job = new SysJobEntity();
		job.setTaskType(SysJobTaskType.CUSTOM_CLASS.name());
		job.setInvokeTarget(null);

		// 断言抛出统一任务类型不支持异常。
		assertThatThrownBy(() -> registry.resolve(job)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_TASK_TYPE_UNSUPPORTED);
	}

}
