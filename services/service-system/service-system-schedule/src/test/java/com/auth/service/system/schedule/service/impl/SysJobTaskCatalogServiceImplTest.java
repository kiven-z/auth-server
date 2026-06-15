package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.example.bean.BeanInvokeDemoTask;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;
import com.auth.service.system.schedule.support.QuartzTaskRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CLASS_NOT_WHITELISTED;
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

	private static final String DEMO_CLASS = BeanInvokeDemoTask.class.getName();

	@Mock
	private QuartzTaskRegistry quartzTaskRegistry;

	private SysJobTaskCatalogServiceImpl sysJobTaskCatalogService;

	@BeforeEach
	void setUp() {
		sysJobTaskCatalogService = new SysJobTaskCatalogServiceImpl(quartzTaskRegistry);
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

}
