package com.auth.service.system.schedule.task;

import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.executor.BeanInvokeTaskExecutor;
import com.auth.service.system.schedule.task.executor.CustomClassTaskExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysJobTaskExecutorRegistry} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobTaskExecutorRegistry 执行器注册")
class SysJobTaskExecutorRegistryTest {

	@Test
	@DisplayName("resolve：按 taskType 返回已注册执行器")
	void resolveReturnsRegisteredExecutor() {
		BeanInvokeTaskExecutor beanInvokeTaskExecutor = new BeanInvokeTaskExecutor(null);
		CustomClassTaskExecutor customClassTaskExecutor = new CustomClassTaskExecutor();
		SysJobTaskExecutorRegistry registry = new SysJobTaskExecutorRegistry(
				List.of(beanInvokeTaskExecutor, customClassTaskExecutor));

		assertThat(registry.resolve(SysJobTaskType.BEAN_INVOKE)).isSameAs(beanInvokeTaskExecutor);
		assertThat(registry.resolve(SysJobTaskType.CUSTOM_CLASS)).isSameAs(customClassTaskExecutor);
	}

}
