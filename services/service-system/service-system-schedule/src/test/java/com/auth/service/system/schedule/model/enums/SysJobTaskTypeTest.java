package com.auth.service.system.schedule.model.enums;

import com.auth.service.system.schedule.task.mode.bean.BeanInvokeEntryJob;
import com.auth.service.system.schedule.task.mode.custom.DelegatingCustomEntryJob;
import com.auth.service.system.schedule.task.mode.message.MessageDispatchEntryJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysJobTaskType} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobTaskType 任务类型枚举")
class SysJobTaskTypeTest {

	@Test
	@DisplayName("defaultHandlerCode：各类型返回约定编码")
	void defaultHandlerCodeReturnsExpectedCodes() {
		assertThat(SysJobTaskType.BEAN_INVOKE.defaultHandlerCode()).isEqualTo("beanInvokeHandler");
		assertThat(SysJobTaskType.CUSTOM_CLASS.defaultHandlerCode()).isEqualTo("customClassHandler");
		assertThat(SysJobTaskType.MESSAGE_DISPATCH.defaultHandlerCode()).isEqualTo("messageDispatchHandler");
	}

	@Test
	@DisplayName("resolveEntryJobClass：并发与禁止并发返回对应入口 Job")
	void resolveEntryJobClassReturnsMatchedEntryJobs() {
		assertThat(SysJobTaskType.BEAN_INVOKE.resolveEntryJobClass(true)).isEqualTo(BeanInvokeEntryJob.class);
		assertThat(SysJobTaskType.BEAN_INVOKE.resolveEntryJobClass(false)).isEqualTo(BeanInvokeEntryJob.Disallow.class);

		assertThat(SysJobTaskType.CUSTOM_CLASS.resolveEntryJobClass(true)).isEqualTo(DelegatingCustomEntryJob.class);
		assertThat(SysJobTaskType.CUSTOM_CLASS.resolveEntryJobClass(false))
			.isEqualTo(DelegatingCustomEntryJob.Disallow.class);

		assertThat(SysJobTaskType.MESSAGE_DISPATCH.resolveEntryJobClass(true)).isEqualTo(MessageDispatchEntryJob.class);
		assertThat(SysJobTaskType.MESSAGE_DISPATCH.resolveEntryJobClass(false))
			.isEqualTo(MessageDispatchEntryJob.Disallow.class);
	}

}
