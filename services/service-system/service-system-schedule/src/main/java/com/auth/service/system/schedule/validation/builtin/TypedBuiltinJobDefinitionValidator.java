package com.auth.service.system.schedule.validation.builtin;

import com.auth.service.system.schedule.support.builtin.BuiltinJobParamsParser;
import org.springframework.lang.NonNull;

/**
 * 按 Job 类与载荷类型委托 {@link BuiltinJobParamsParser} 的通用校验器
 *
 * @author Bunny
 */
public class TypedBuiltinJobDefinitionValidator implements BuiltinJobDefinitionValidator {

	private final String jobClassName;

	private final Class<?> paramsType;

	private final BuiltinJobParamsParser builtinJobParamsParser;

	/**
	 * @param jobClass 内置 Job 类型
	 * @param paramsType {@code job_params} 载荷类型
	 * @param builtinJobParamsParser 参数解析器
	 */
	public TypedBuiltinJobDefinitionValidator(@NonNull Class<?> jobClass, @NonNull Class<?> paramsType,
			@NonNull BuiltinJobParamsParser builtinJobParamsParser) {
		this.jobClassName = jobClass.getName();
		this.paramsType = paramsType;
		this.builtinJobParamsParser = builtinJobParamsParser;
	}

	@Override
	public String jobClass() {
		return jobClassName;
	}

	@Override
	public void validateJobParams(String jobParamsJson) {
		builtinJobParamsParser.parseAndValidate(jobParamsJson, paramsType);
	}

}
