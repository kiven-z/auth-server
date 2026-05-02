package com.auth.service.system.schedule.task.custom;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * 自定义 Job 示例：从 JobDataMap 读取 job_params 合并的参数并执行
 *
 * <p>
 * 配置示例：
 * </p>
 * <ul>
 * <li>invoke_target：留空（自定义 Job 模式）</li>
 * <li>job_class：{@code com.auth.service.system.schedule.task.custom.EchoParamsJob}</li>
 * <li>job_params：{@code {"message":"hello","repeat":3}}</li>
 * </ul>
 *
 * @author Bunny
 */
@QuartzTask(name = "EchoParamsJob", description = "示例：读取 job_params(message/repeat) 并输出日志")
@Slf4j
@Component
public class EchoParamsJob implements Job {

	private static String readString(JobDataMap map) {
		Object v = map.get("message");
		if (v == null) {
			return "default-message";
		}
		String s = String.valueOf(v);
		return CharSequenceUtil.isBlank(s) ? "default-message" : s.trim();
	}

	private static int readInt(JobDataMap map) {
		Object v = map.get("repeat");
		if (v == null) {
			return 1;
		}
		if (v instanceof Number n) {
			return n.intValue();
		}
		try {
			return Integer.parseInt(String.valueOf(v).trim());
		}
		catch (Exception exception) {
			return 1;
		}
	}

	@Override
	public void execute(JobExecutionContext context) {
		JobDataMap map = context.getMergedJobDataMap();
		String message = readString(map);
		int repeat = readInt(map);
		if (repeat < 1) {
			repeat = 1;
		}
		if (repeat > 20) {
			repeat = 20;
		}
		for (int i = 0; i < repeat; i++) {
			log.info("EchoParamsJob message={} idx={}", message, i + 1);
		}
	}

}
