package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.schedule.model.payload.MessageDispatchPayload;
import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;
import com.auth.service.system.schedule.support.MessageDispatchCommandParser;
import com.auth.service.system.schedule.support.QuartzTaskRegistry;
import com.auth.service.system.schedule.task.custom.DemoQuartzTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.JOB_CLASS_NOT_WHITELISTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobTaskCatalogServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobTaskCatalogServiceImpl 任务目录")
@ExtendWith(MockitoExtension.class)
class SysJobTaskCatalogServiceImplTest {

	@Mock
	private QuartzTaskRegistry quartzTaskRegistry;

	@Mock
	private ApplicationContext applicationContext;

	@Mock
	private MessageDispatchCommandParser messageDispatchCommandParser;

	@Test
	@DisplayName("listQuartzClasses 返回白名单类列表")
	void listQuartzClassesReturnsRegistryItems() {
		QuartzTaskClassVO classVO = new QuartzTaskClassVO();
		classVO.setClassName(DemoQuartzTask.class.getName());
		when(quartzTaskRegistry.listAll()).thenReturn(List.of(classVO));
		SysJobTaskCatalogServiceImpl service = new SysJobTaskCatalogServiceImpl(quartzTaskRegistry, applicationContext,
				messageDispatchCommandParser);

		List<QuartzTaskClassVO> result = service.listQuartzClasses();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getClassName()).isEqualTo(DemoQuartzTask.class.getName());
	}

	@Test
	@DisplayName("listCallableMethods 仅返回可调用方法并提供 invoke_target 示例")
	void listCallableMethodsReturnsCallableMethods() {
		when(quartzTaskRegistry.isAllowed(DemoQuartzTask.class.getName())).thenReturn(true);
		when(applicationContext.getBeanNamesForType(DemoQuartzTask.class))
			.thenReturn(new String[] { "demoQuartzTask" });
		SysJobTaskCatalogServiceImpl service = new SysJobTaskCatalogServiceImpl(quartzTaskRegistry, applicationContext,
				messageDispatchCommandParser);

		List<QuartzTaskMethodVO> methods = service.listCallableMethods(DemoQuartzTask.class.getName());

		assertThat(methods).extracting(QuartzTaskMethodVO::getMethodName).contains("run", "echo");
		assertThat(methods).extracting(QuartzTaskMethodVO::getInvokeTargetExample)
			.contains("demoQuartzTask.run()", "demoQuartzTask.echo('demo')");
	}

	@Test
	@DisplayName("listCallableMethods 非白名单类时抛出 JOB_CLASS_NOT_WHITELISTED")
	void listCallableMethodsThrowsWhenClassNotWhitelisted() {
		when(quartzTaskRegistry.isAllowed(DemoQuartzTask.class.getName())).thenReturn(false);
		SysJobTaskCatalogServiceImpl service = new SysJobTaskCatalogServiceImpl(quartzTaskRegistry, applicationContext,
				messageDispatchCommandParser);

		assertThatThrownBy(() -> service.listCallableMethods(DemoQuartzTask.class.getName()))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(JOB_CLASS_NOT_WHITELISTED);
	}

	@Test
	@DisplayName("messageDispatchPayloadTemplate 返回标准模板")
	void messageDispatchPayloadTemplateReturnsTemplate() {
		MessageDispatchPayload payload = new MessageDispatchPayload();
		when(messageDispatchCommandParser.buildTemplatePayload()).thenReturn(payload);
		SysJobTaskCatalogServiceImpl service = new SysJobTaskCatalogServiceImpl(quartzTaskRegistry, applicationContext,
				messageDispatchCommandParser);

		MessageDispatchPayload result = service.messageDispatchPayloadTemplate();

		assertThat(result).isSameAs(payload);
	}

}
