package com.auth.service.system.schedule.validation;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_TASK_TYPE_UNSUPPORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SysJobDefinitionValidationManager} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobDefinitionValidationManager 任务定义校验管理器")
class SysJobDefinitionValidationManagerTest {

	@Test
	@DisplayName("normalizeAndValidate：按任务类型归一化并调用对应校验器")
	void normalizeAndValidateRoutesToMatchedValidator() {
		RecorderValidator beanValidator = new RecorderValidator(SysJobTaskType.BEAN_INVOKE);
		RecorderValidator customValidator = new RecorderValidator(SysJobTaskType.CUSTOM_CLASS);
		SysJobDefinitionValidationManager manager = new SysJobDefinitionValidationManager(
				List.of(beanValidator, customValidator));

		SysJobEntity job = new SysJobEntity();
		job.setTaskType(SysJobTaskType.BEAN_INVOKE.name());
		job.setInvokeTarget("beanInvokeDemoTask.run()");

		manager.normalizeAndValidate(job);

		assertThat(job.getTaskType()).isEqualTo(SysJobTaskType.BEAN_INVOKE.name());
		assertThat(job.getHandlerCode()).isEqualTo("beanInvokeHandler");
		assertThat(beanValidator.invoked).isTrue();
		assertThat(customValidator.invoked).isFalse();
	}

	@Test
	@DisplayName("normalizeAndValidate：缺少对应类型校验器时抛出 JOB_TASK_TYPE_UNSUPPORTED")
	void normalizeAndValidateThrowsWhenValidatorMissing() {
		SysJobDefinitionValidationManager manager = new SysJobDefinitionValidationManager(
				List.of(new RecorderValidator(SysJobTaskType.BEAN_INVOKE)));

		SysJobEntity job = new SysJobEntity();
		job.setTaskType(SysJobTaskType.CUSTOM_CLASS.name());

		assertThatThrownBy(() -> manager.normalizeAndValidate(job)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_TASK_TYPE_UNSUPPORTED);
	}

	/**
	 * 记录调用轨迹的测试校验器
	 */
	private static class RecorderValidator implements SysJobTaskTypeDefinitionValidator {

		private final SysJobTaskType type;

		private boolean invoked;

		private RecorderValidator(SysJobTaskType type) {
			this.type = type;
		}

		@Override
		public SysJobTaskType taskType() {
			return type;
		}

		@Override
		public void validate(SysJobEntity job) {
			this.invoked = true;
		}

	}

}
