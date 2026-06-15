package com.auth.service.system.schedule.task.builtin.validation;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.task.builtin.http.HttpGetInvokeJob;
import com.auth.service.system.schedule.task.builtin.http.validation.HttpGetBuiltinJobDefinitionValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BuiltinJobDefinitionValidatorRegistry} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("BuiltinJobDefinitionValidatorRegistry 内置 job_params 校验注册")
@ExtendWith(MockitoExtension.class)
class BuiltinJobDefinitionValidatorRegistryTest {

	@Mock
	private HttpGetBuiltinJobDefinitionValidator httpGetValidator;

	@Test
	@DisplayName("validateIfPresent：匹配内置类时应委派校验器")
	void validateIfPresent_whenBuiltinClass_delegatesValidator() {
		when(httpGetValidator.jobClass()).thenReturn(HttpGetInvokeJob.class.getName());
		BuiltinJobDefinitionValidatorRegistry registry = new BuiltinJobDefinitionValidatorRegistry(
				List.of(httpGetValidator));

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpGetInvokeJob.class.getName());
		job.setJobParams("{\"version\":1,\"url\":\"https://example.com\"}");

		registry.validateIfPresent(job);

		verify(httpGetValidator).validateJobParams(job.getJobParams());
	}

	@Test
	@DisplayName("validateIfPresent：非内置类时应跳过")
	void validateIfPresent_whenUnknownClass_skips() {
		BuiltinJobDefinitionValidatorRegistry registry = new BuiltinJobDefinitionValidatorRegistry(
				List.of(httpGetValidator));

		SysJobEntity job = new SysJobEntity();
		job.setJobClass("com.example.OtherJob");

		registry.validateIfPresent(job);

		verify(httpGetValidator, never()).validateJobParams(org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	@DisplayName("validateIfPresent：内置类参数非法时应抛出 SysJobException")
	void validateIfPresent_whenParamsInvalid_propagates() {
		when(httpGetValidator.jobClass()).thenReturn(HttpGetInvokeJob.class.getName());
		doThrow(new SysJobException(
				com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_PARAMS_JSON_INVALID, "bad"))
			.when(httpGetValidator)
			.validateJobParams("bad");
		BuiltinJobDefinitionValidatorRegistry registry = new BuiltinJobDefinitionValidatorRegistry(
				List.of(httpGetValidator));

		SysJobEntity job = new SysJobEntity();
		job.setJobClass(HttpGetInvokeJob.class.getName());
		job.setJobParams("bad");

		assertThatThrownBy(() -> registry.validateIfPresent(job)).isInstanceOf(SysJobException.class);
	}

}
