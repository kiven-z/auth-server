package com.auth.service.system.schedule.task.builtin.http.validation;

import com.auth.service.system.schedule.task.builtin.http.HttpGetInvokeJob;
import com.auth.service.system.schedule.task.builtin.http.model.HttpGetJobParams;
import com.auth.service.system.schedule.task.builtin.support.BuiltinJobParamsParser;
import com.auth.service.system.schedule.task.builtin.validation.BuiltinJobDefinitionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * HTTP GET 内置任务定义校验器（保存/更新时校验 {@code job_params}）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class HttpGetBuiltinJobDefinitionValidator implements BuiltinJobDefinitionValidator {

	private final BuiltinJobParamsParser builtinJobParamsParser;

	@Override
	public String jobClass() {
		return HttpGetInvokeJob.class.getName();
	}

	@Override
	public void validateJobParams(String jobParamsJson) {
		builtinJobParamsParser.parseAndValidate(jobParamsJson, HttpGetJobParams.class);
	}

}
