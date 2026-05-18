package com.auth.service.system.schedule.task.custom;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 带 Spring 注入的自定义 Job 示例
 *
 * @author Bunny
 */
@QuartzTask(name = "SpringInjectDemoJob", description = "示例：注入 RestTemplate 并发起 GET 请求")
@RequiredArgsConstructor
@Slf4j
@Component
public class SpringInjectDemoJob implements Job {

	private final RestTemplate restTemplate;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap map = context.getMergedJobDataMap();
		String url = java.util.Objects.toString(map.get("url"), null);
		if (CharSequenceUtil.isBlank(url)) {
			url = "https://httpbin.org/get";
		}
		try {
			String resp = restTemplate.getForObject(java.util.Objects.requireNonNull(url, "url"), String.class);
			log.info("SpringInjectDemoJob ok url={} respLen={}", url, resp == null ? 0 : resp.length());
		}
		catch (Exception e) {
			log.warn("SpringInjectDemoJob failed url={} err={}", url, e.getMessage());
			throw new JobExecutionException(e);
		}
	}

}
