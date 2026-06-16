package com.auth.service.system.schedule.catalog;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.support.catalog.SysJobPayloadTemplateProvider;
import com.auth.service.system.schedule.support.catalog.SysJobPayloadTemplateRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_PAYLOAD_TEMPLATE_UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SysJobPayloadTemplateRegistry} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobPayloadTemplateRegistry 载荷模板注册")
class SysJobPayloadTemplateRegistryTest {

	@Test
	@DisplayName("buildTemplate：按 taskType 返回已注册提供者模板")
	void buildTemplateReturnsRegisteredProviderPayload() {
		Object template = new Object();
		SysJobPayloadTemplateProvider provider = new StubProvider(SysJobTaskType.MESSAGE_DISPATCH, template);
		SysJobPayloadTemplateRegistry registry = new SysJobPayloadTemplateRegistry(List.of(provider));

		assertThat(registry.buildTemplate(SysJobTaskType.MESSAGE_DISPATCH)).isSameAs(template);
	}

	@Test
	@DisplayName("buildTemplate：未注册 taskType 抛出 JOB_PAYLOAD_TEMPLATE_UNAVAILABLE")
	void buildTemplateRejectsUnregisteredTaskType() {
		SysJobPayloadTemplateRegistry registry = new SysJobPayloadTemplateRegistry(List.of());

		assertThatThrownBy(() -> registry.buildTemplate(SysJobTaskType.BEAN_INVOKE)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_TEMPLATE_UNAVAILABLE);
	}

	private record StubProvider(SysJobTaskType taskType, Object template) implements SysJobPayloadTemplateProvider {

		@Override
		public Object buildTemplatePayload() {
			return template;
		}

	}

}
