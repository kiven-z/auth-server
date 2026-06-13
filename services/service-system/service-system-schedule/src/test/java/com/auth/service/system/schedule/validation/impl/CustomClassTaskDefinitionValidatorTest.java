package com.auth.service.system.schedule.validation.impl;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.support.QuartzTaskRegistry;
import com.auth.service.system.schedule.task.mode.custom.job.SpringInjectDemoJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CLASS_LOAD_FAILED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CLASS_NOT_WHITELISTED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link CustomClassTaskDefinitionValidator} 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomClassTaskDefinitionValidator 自定义类任务校验")
class CustomClassTaskDefinitionValidatorTest {

	@Mock
	private QuartzTaskRegistry quartzTaskRegistry;

	@InjectMocks
	private CustomClassTaskDefinitionValidator validator;

	@Test
	@DisplayName("validate：白名单且可加载的 job_class 应通过")
	void validate_whenClassAllowedAndLoadable_passes() {
		String className = SpringInjectDemoJob.class.getName();
		when(quartzTaskRegistry.isAllowed(className)).thenReturn(true);

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(className);

		validator.validate(job);
	}

	@Test
	@DisplayName("validate：不在白名单的 job_class 应抛出 JOB_CLASS_NOT_WHITELISTED")
	void validate_whenClassNotWhitelisted_throws() {
		String className = "com.example.MissingJob";
		when(quartzTaskRegistry.isAllowed(className)).thenReturn(false);

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(className);

		assertThatThrownBy(() -> validator.validate(job)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_CLASS_NOT_WHITELISTED);
	}

	@Test
	@DisplayName("validate：白名单内但类无法加载时应抛出 JOB_CLASS_LOAD_FAILED")
	void validate_whenClassNotFound_throws() {
		String className = "com.auth.service.system.schedule.task.custom.SpringInjectDemoJob";
		when(quartzTaskRegistry.isAllowed(className)).thenReturn(true);

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(className);

		assertThatThrownBy(() -> validator.validate(job)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_CLASS_LOAD_FAILED);
	}

}
