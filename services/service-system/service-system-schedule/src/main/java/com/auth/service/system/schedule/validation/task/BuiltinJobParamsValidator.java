package com.auth.service.system.schedule.validation.task;

import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.task.builtin.FeignInvokeJob;
import com.auth.service.system.schedule.task.builtin.HttpInvokeJob;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 内置 Job {@code job_params} 定义层校验（保存/更新任务时触发）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class BuiltinJobParamsValidator {

	private static final Map<String, Class<?>> JOB_CLASS_TO_PARAMS = Map.of(HttpInvokeJob.class.getName(),
			HttpInvokeJobParams.class, FeignInvokeJob.class.getName(), FeignInvokeJobParams.class);

	private final BuiltinJobParamsParser builtinJobParamsParser;

	/**
	 * 若 {@code job_class} 为已知内置 Job，则校验 {@code job_params}
	 * @param job 任务定义
	 */
	public void validateIfPresent(SysJobEntity job) {
		String jobClass = job.getJobClass();

		Class<?> paramsType = JOB_CLASS_TO_PARAMS.get(jobClass);
		if (paramsType == null) {
			return;
		}

		//
		String jobParams = job.getJobParams();
		builtinJobParamsParser.parseAndValidate(jobParams, paramsType);
	}

}
