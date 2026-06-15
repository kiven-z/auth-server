package com.auth.service.system.schedule.task.builtin.feign.validation;

import com.auth.service.system.schedule.task.builtin.feign.FeignInvokeJob;
import com.auth.service.system.schedule.task.builtin.feign.model.FeignInvokeJobParams;
import com.auth.service.system.schedule.task.builtin.support.BuiltinJobParamsParser;
import com.auth.service.system.schedule.task.builtin.validation.BuiltinJobDefinitionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 微服务远程调用内置任务定义校验器（保存/更新时校验 {@code job_params}）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class FeignInvokeBuiltinJobDefinitionValidator implements BuiltinJobDefinitionValidator {

	private final BuiltinJobParamsParser builtinJobParamsParser;

	@Override
	public String jobClass() {
		return FeignInvokeJob.class.getName();
	}

	@Override
	public void validateJobParams(String jobParamsJson) {
		builtinJobParamsParser.parseAndValidate(jobParamsJson, FeignInvokeJobParams.class);
	}

}
