package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;
import com.auth.service.system.schedule.support.catalog.QuartzTaskRegistry;
import com.auth.service.system.schedule.support.catalog.SysJobPayloadTemplateRegistry;
import com.auth.service.system.schedule.task.example.BeanInvokeDemoTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobTaskCatalogServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobTaskCatalogServiceImpl 任务目录与载荷模板")
@ExtendWith(MockitoExtension.class)
class SysJobTaskCatalogServiceImplTest {

	private static final String DEMO_CLASS = BeanInvokeDemoTask.class.getName();

	@Mock
	private QuartzTaskRegistry quartzTaskRegistry;

	@Mock
	private SysJobPayloadTemplateRegistry sysJobPayloadTemplateRegistry;

	private SysJobTaskCatalogServiceImpl sysJobTaskCatalogService;

	@BeforeEach
	void setUp() {
		sysJobTaskCatalogService = new SysJobTaskCatalogServiceImpl(quartzTaskRegistry, sysJobPayloadTemplateRegistry);
	}

	@Test
	@DisplayName("listQuartzClasses：委托 QuartzTaskRegistry 返回白名单类列表")
	void listQuartzClassesDelegatesToRegistry() {
		QuartzTaskClassVO classVo = new QuartzTaskClassVO();
		classVo.setClassName(DEMO_CLASS);
		when(quartzTaskRegistry.listAll()).thenReturn(List.of(classVo));

		List<QuartzTaskClassVO> actual = sysJobTaskCatalogService.listQuartzClasses();

		assertThat(actual).containsExactly(classVo);
		verify(quartzTaskRegistry).listAll();
	}

	@Test
	@DisplayName("listCallableMethods：className 为空时抛出 PARAM_REQUIRED")
	void listCallableMethodsRejectsBlankClassName() {
		assertThatThrownBy(() -> sysJobTaskCatalogService.listCallableMethods(" ")).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);
	}

	@Test
	@DisplayName("listCallableMethods：白名单类返回声明的无参与 String 方法")
	void listCallableMethodsReturnsDeclaredMethods() {
		when(quartzTaskRegistry.isAllowed(DEMO_CLASS)).thenReturn(true);
		when(quartzTaskRegistry.resolveBeanName(DEMO_CLASS)).thenReturn("beanInvokeDemoTask");

		List<QuartzTaskMethodVO> methods = sysJobTaskCatalogService.listCallableMethods(DEMO_CLASS);

		assertThat(methods).extracting(QuartzTaskMethodVO::getMethodName).contains("run", "runWithMessage");
		QuartzTaskMethodVO runWithMessage = methods.stream()
			.filter(item -> "runWithMessage".equals(item.getMethodName()))
			.findFirst()
			.orElseThrow();
		assertThat(runWithMessage.getInvokeTargetExample()).isEqualTo("beanInvokeDemoTask.runWithMessage('')");
		assertThat(runWithMessage.getRequiresStringParam()).isTrue();
	}

	@Test
	@DisplayName("listCallableMethods：非白名单类抛出 JOB_CLASS_NOT_WHITELISTED")
	void listCallableMethodsRejectsNonWhitelistedClass() {
		when(quartzTaskRegistry.isAllowed(DEMO_CLASS)).thenReturn(false);

		assertThatThrownBy(() -> sysJobTaskCatalogService.listCallableMethods(DEMO_CLASS))
			.isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_CLASS_NOT_WHITELISTED);
	}

	@Test
	@DisplayName("resolvePayloadTemplate：解析 taskType 并委托注册中心")
	void resolvePayloadTemplateDelegatesToRegistry() {
		Object template = new Object();
		when(sysJobPayloadTemplateRegistry.buildTemplate(SysJobTaskType.MESSAGE_DISPATCH)).thenReturn(template);

		Object actual = sysJobTaskCatalogService.resolvePayloadTemplate("MESSAGE_DISPATCH");

		assertThat(actual).isSameAs(template);
		verify(sysJobPayloadTemplateRegistry).buildTemplate(SysJobTaskType.MESSAGE_DISPATCH);
	}

	@Test
	@DisplayName("resolvePayloadTemplate：非法 taskType 抛出 JOB_TASK_TYPE_UNSUPPORTED")
	void resolvePayloadTemplateRejectsUnknownTaskType() {
		assertThatThrownBy(() -> sysJobTaskCatalogService.resolvePayloadTemplate("UNKNOWN"))
			.isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_TASK_TYPE_UNSUPPORTED);
	}

	@Test
	@DisplayName("resolvePayloadTemplate：未注册模板抛出 JOB_PAYLOAD_TEMPLATE_UNAVAILABLE")
	void resolvePayloadTemplatePropagatesRegistryException() {
		when(sysJobPayloadTemplateRegistry.buildTemplate(SysJobTaskType.BEAN_INVOKE))
			.thenThrow(new SysJobException(JOB_PAYLOAD_TEMPLATE_UNAVAILABLE, SysJobTaskType.BEAN_INVOKE.name()));

		assertThatThrownBy(() -> sysJobTaskCatalogService.resolvePayloadTemplate("BEAN_INVOKE"))
			.isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_PAYLOAD_TEMPLATE_UNAVAILABLE);
	}

}
