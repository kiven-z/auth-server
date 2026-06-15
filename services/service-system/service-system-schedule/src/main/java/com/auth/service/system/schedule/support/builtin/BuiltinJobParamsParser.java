package com.auth.service.system.schedule.support.builtin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.jobparams.VersionedJobParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_PARAMS_JSON_INVALID;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_PAYLOAD_VERSION_UNSUPPORTED;

/**
 * 内置任务 {@code job_params} 通用解析与 Bean Validation 校验
 *
 * @author Bunny
 */
@Component
public class BuiltinJobParamsParser {

	private final ObjectMapper objectMapper;

	private final Validator validator;

	public BuiltinJobParamsParser(ObjectMapper objectMapper, Validator validator) {
		this.objectMapper = objectMapper;
		this.validator = validator;
	}

	/**
	 * 校验版本化载荷协议版本
	 * @param versioned 版本化载荷
	 */
	private static void assertSupportedVersion(VersionedJobParams versioned) {
		Integer version = versioned.getVersion();
		if (version == null || version != VersionedJobParams.VERSION_1) {
			throw new SysJobException(JOB_PAYLOAD_VERSION_UNSUPPORTED, String.valueOf(version));
		}
	}

	/**
	 * 从执行上下文读取并解析 {@code job_params}
	 * @param context Quartz 执行上下文
	 * @param paramsType 载荷类型
	 * @param <T> 载荷类型
	 * @return 校验后的载荷
	 */
	public <T> T parseFromContext(JobExecutionContext context, Class<T> paramsType) {
		return parseAndValidate(BuiltinJobParamsSupport.requireRawJson(context), paramsType);
	}

	/**
	 * 解析并校验 {@code job_params} JSON
	 * @param jobParamsJson 原始 JSON
	 * @param paramsType 载荷类型
	 * @param <T> 载荷类型
	 * @return 校验后的载荷
	 */
	public <T> T parseAndValidate(String jobParamsJson, Class<T> paramsType) {
		if (CharSequenceUtil.isBlank(jobParamsJson)) {
			throw new SysJobException(PARAM_REQUIRED, "job_params");
		}
		try {
			T params = objectMapper.readValue(jobParamsJson, paramsType);
			validateBean(params);
			if (params instanceof VersionedJobParams versioned) {
				assertSupportedVersion(versioned);
			}
			return params;
		}
		catch (SysJobException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new SysJobException(JOB_PARAMS_JSON_INVALID, exception.getMessage());
		}
	}

	/**
	 * Bean Validation 校验
	 * @param params 载荷对象
	 * @param <T> 载荷类型
	 */
	private <T> void validateBean(T params) {
		Set<ConstraintViolation<T>> violations = validator.validate(params);
		if (CollUtil.isNotEmpty(violations)) {
			String message = violations.iterator().next().getMessage();
			throw new SysJobException(JOB_PARAMS_JSON_INVALID, message);
		}
	}

}
