package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.catalog.SysJobPayloadTemplateRegistry;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_PAYLOAD_TEMPLATE_UNAVAILABLE;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_TASK_TYPE_UNSUPPORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobPayloadTemplateServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobPayloadTemplateServiceImpl 载荷模板服务")
@ExtendWith(MockitoExtension.class)
class SysJobPayloadTemplateServiceImplTest {

	@Mock
	private SysJobPayloadTemplateRegistry sysJobPayloadTemplateRegistry;

	private SysJobPayloadTemplateServiceImpl sysJobPayloadTemplateService;

	@BeforeEach
	void setUp() {
		sysJobPayloadTemplateService = new SysJobPayloadTemplateServiceImpl(sysJobPayloadTemplateRegistry);
	}

	@Test
	@DisplayName("resolvePayloadTemplate：解析 taskType 并委托注册中心")
	void resolvePayloadTemplateDelegatesToRegistry() {
		Object template = new Object();
		when(sysJobPayloadTemplateRegistry.buildTemplate(SysJobTaskType.MESSAGE_DISPATCH)).thenReturn(template);

		Object actual = sysJobPayloadTemplateService.resolvePayloadTemplate("MESSAGE_DISPATCH");

		assertThat(actual).isSameAs(template);
		verify(sysJobPayloadTemplateRegistry).buildTemplate(SysJobTaskType.MESSAGE_DISPATCH);
	}

	@Test
	@DisplayName("resolvePayloadTemplate：非法 taskType 抛出 JOB_TASK_TYPE_UNSUPPORTED")
	void resolvePayloadTemplateRejectsUnknownTaskType() {
		assertThatThrownBy(() -> sysJobPayloadTemplateService.resolvePayloadTemplate("UNKNOWN"))
			.isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_TASK_TYPE_UNSUPPORTED);
	}

	@Test
	@DisplayName("resolvePayloadTemplate：未注册模板抛出 JOB_PAYLOAD_TEMPLATE_UNAVAILABLE")
	void resolvePayloadTemplatePropagatesRegistryException() {
		when(sysJobPayloadTemplateRegistry.buildTemplate(SysJobTaskType.BEAN_INVOKE))
			.thenThrow(new SysJobException(JOB_PAYLOAD_TEMPLATE_UNAVAILABLE, SysJobTaskType.BEAN_INVOKE.name()));

		assertThatThrownBy(() -> sysJobPayloadTemplateService.resolvePayloadTemplate("BEAN_INVOKE"))
			.isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_TEMPLATE_UNAVAILABLE);
	}

}
