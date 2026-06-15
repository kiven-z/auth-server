package com.auth.service.system.schedule.support.builtin;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import lombok.experimental.UtilityClass;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;

/**
 * 从 Quartz {@link JobDataMap} 读取内置任务 {@code job_params} 原始 JSON
 *
 * @author Bunny
 */
@UtilityClass
public class BuiltinJobParamsSupport {

	/**
	 * 读取 {@code job_params} 原始 JSON（非空）
	 * @param context Quartz 执行上下文
	 * @return 原始 JSON 字符串
	 */
	public static String requireRawJson(JobExecutionContext context) {
		JobDataMap jobDataMap = context.getMergedJobDataMap();
		String raw = jobDataMap.getString(SysJobQuartzDataKeys.JOB_PARAMS);
		if (CharSequenceUtil.isBlank(raw)) {
			throw new SysJobException(PARAM_REQUIRED, "job_params");
		}
		return raw;
	}

}
