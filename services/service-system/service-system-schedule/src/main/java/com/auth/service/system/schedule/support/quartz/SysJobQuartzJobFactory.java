package com.auth.service.system.schedule.support.quartz;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.stereotype.Component;

/**
 * 根据 {@link SysJobEntity} 构建 Quartz {@link JobDetail}
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SysJobQuartzJobFactory {

	/**
	 * 合并 JSON 参数
	 * @param map 参数映射
	 * @param jobParamsJson 参数 JSON
	 */
	private static void mergeJsonParams(JobDataMap map, String jobParamsJson) {
		if (CharSequenceUtil.isBlank(jobParamsJson)) {
			return;
		}
		try {
			JSONObject json = JSONUtil.parseObj(jobParamsJson);
			map.putAll(json);
		}
		catch (Exception exception) {
			log.warn("解析 job_params 失败: {}", jobParamsJson, exception);
			map.put("jobParamsRaw", jobParamsJson);
		}
	}

	/**
	 * 构建 JobDetail
	 * @param job 任务实体
	 * @return JobDetail
	 */
	public JobDetail build(SysJobEntity job) {
		String jobName = job.getJobName();
		String jobGroup = job.getJobGroup();
		SysJobTaskType resolvedTaskType = SysJobTaskType.resolve(job.getTaskType());
		boolean concurrent = BooleanUtil.isTrue(job.getConcurrent());
		Class<? extends Job> jobClass = resolvedTaskType.resolveEntryJobClass(concurrent);

		JobDataMap map = new JobDataMap();
		map.put(SysJobQuartzDataKeys.SYS_JOB_ID, String.valueOf(job.getId()));
		map.put(SysJobQuartzDataKeys.INVOKE_TARGET, job.getInvokeTarget());
		map.put(SysJobQuartzDataKeys.JOB_CLASS, job.getJobClass());
		map.put(SysJobQuartzDataKeys.TASK_TYPE, resolvedTaskType.name());
		String handlerCode = CharSequenceUtil.isNotBlank(job.getHandlerCode()) ? job.getHandlerCode()
				: resolvedTaskType.defaultHandlerCode();
		map.put(SysJobQuartzDataKeys.HANDLER_CODE, handlerCode);
		map.put(SysJobQuartzDataKeys.PAYLOAD_JSON, job.getPayloadJson());
		map.put(SysJobQuartzDataKeys.TRIGGER_TYPE, SysJobQuartzDataKeys.TRIGGER_TYPE_SCHEDULE);
		mergeJsonParams(map, job.getJobParams());

		return JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroup).usingJobData(map).storeDurably().build();
	}

}
