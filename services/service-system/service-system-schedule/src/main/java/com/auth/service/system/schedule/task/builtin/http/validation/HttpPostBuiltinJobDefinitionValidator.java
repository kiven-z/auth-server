package com.auth.service.system.schedule.task.builtin.http.validation;

import com.auth.service.system.schedule.task.builtin.http.HttpPostInvokeJob;
import com.auth.service.system.schedule.task.builtin.http.model.HttpPostJobParams;
import com.auth.service.system.schedule.task.builtin.support.BuiltinJobParamsParser;
import com.auth.service.system.schedule.task.builtin.validation.BuiltinJobDefinitionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * HTTP POST 内置任务定义校验器（保存/更新时校验 {@code job_params}）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class HttpPostBuiltinJobDefinitionValidator implements BuiltinJobDefinitionValidator {

	private final BuiltinJobParamsParser builtinJobParamsParser;

	@Override
	public String jobClass() {
		return HttpPostInvokeJob.class.getName();
	}

	@Override
	public void validateJobParams(String jobParamsJson) {
		builtinJobParamsParser.parseAndValidate(jobParamsJson, HttpPostJobParams.class);
	}

}
