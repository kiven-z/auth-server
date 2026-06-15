package com.auth.service.system.schedule.validation.builtin;

import com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.support.builtin.BuiltinJobParamsParser;
import com.auth.service.system.schedule.task.builtin.FeignInvokeJob;
import com.auth.service.system.schedule.task.builtin.HttpInvokeJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 内置 Job {@code job_params} 校验器集中注册（Job 类 → 载荷类型）
 *
 * @author Bunny
 */
@Configuration
public class BuiltinJobDefinitionValidatorConfig {

	@Bean
	BuiltinJobDefinitionValidator httpInvokeBuiltinJobDefinitionValidator(
			BuiltinJobParamsParser builtinJobParamsParser) {
		return new TypedBuiltinJobDefinitionValidator(HttpInvokeJob.class, HttpInvokeJobParams.class,
				builtinJobParamsParser);
	}

	@Bean
	BuiltinJobDefinitionValidator feignInvokeBuiltinJobDefinitionValidator(
			BuiltinJobParamsParser builtinJobParamsParser) {
		return new TypedBuiltinJobDefinitionValidator(FeignInvokeJob.class, FeignInvokeJobParams.class,
				builtinJobParamsParser);
	}

}
