package com.auth.service.system.schedule.task.builtin.http.validation;

import com.auth.service.system.schedule.task.builtin.http.HttpDeleteInvokeJob;
import com.auth.service.system.schedule.task.builtin.http.model.HttpDeleteJobParams;
import com.auth.service.system.schedule.task.builtin.support.BuiltinJobParamsParser;
import com.auth.service.system.schedule.task.builtin.validation.BuiltinJobDefinitionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * HTTP DELETE 内置任务定义校验器（保存/更新时校验 {@code job_params}）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class HttpDeleteBuiltinJobDefinitionValidator implements BuiltinJobDefinitionValidator {

	private final BuiltinJobParamsParser builtinJobParamsParser;

	@Override
	public String jobClass() {
		return HttpDeleteInvokeJob.class.getName();
	}

	@Override
	public void validateJobParams(String jobParamsJson) {
		builtinJobParamsParser.parseAndValidate(jobParamsJson, HttpDeleteJobParams.class);
	}

}
