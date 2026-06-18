package com.auth.service.system.schedule.validation.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.task.builtin.FeignInvokeJob;
import com.auth.service.system.schedule.task.builtin.HttpInvokeJob;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CONFIG_INVALID;

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

	private final Validator validator;

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

		// 解析并校验 job_params JSON
		String jobParams = job.getJobParams();
		if (CharSequenceUtil.isBlank(jobParams)) {
			throw new SysJobException(PARAM_REQUIRED, "job_params");
		}

		Object params = builtinJobParamsParser.parse(jobParams, paramsType);
		Set<ConstraintViolation<Object>> violations = validator.validate(params);
		if (CollUtil.isNotEmpty(violations)) {
			String message = violations.iterator().next().getMessage();
			throw new SysJobException(JOB_CONFIG_INVALID, "job_params", message);
		}
	}

}
