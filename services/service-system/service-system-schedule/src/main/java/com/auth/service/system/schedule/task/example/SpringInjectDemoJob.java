package com.auth.service.system.schedule.task.example;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * 带 Spring 注入的自定义 Job 示例
 *
 * @author Bunny
 */
@QuartzTask(name = "SpringInjectDemoJob", description = "示例：注入 RestTemplate 并发起 GET 请求",
		modes = { SysJobTaskType.CUSTOM_CLASS })
@RequiredArgsConstructor
@Slf4j
@Component
public class SpringInjectDemoJob implements Job {

	private final RestTemplate restTemplate;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap map = context.getMergedJobDataMap();
		String url = Objects.toString(map.get("url"), null);
		if (CharSequenceUtil.isBlank(url)) {
			url = "https://www.baidu.com/";
		}
		try {
			String resp = restTemplate.getForObject(Objects.requireNonNull(url, "url"), String.class);
			log.info("SpringInjectDemoJob ok url={} respLen={}", url, resp == null ? 0 : resp.length());
		}
		catch (Exception e) {
			log.warn("SpringInjectDemoJob failed url={} err={}", url, e.getMessage());
			throw new JobExecutionException(e);
		}
	}

}
