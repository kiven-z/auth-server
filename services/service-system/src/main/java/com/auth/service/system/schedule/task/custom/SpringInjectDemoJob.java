package com.auth.service.system.schedule.task.custom;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.task.mode.custom.DelegatingCustomEntryJob;
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
 * <p>
 * 用途：验证 {@link DelegatingCustomEntryJob} 通过 Spring
 * {@code AutowireCapableBeanFactory#createBean} 创建 Job 时，依赖注入可用
 * </p>
 *
 * <p>
 * 参数约定（写入 {@code sys_job.job_params} JSON 后会被合并进 JobDataMap）：
 * </p>
 * <ul>
 * <li>url: string（可选，默认 https ://httpbin.org/get）</li>
 * </ul>
 *
 * <p>
 * 配置示例：
 * </p>
 * <ul>
 * <li>invoke_target：留空（自定义 Job 模式）</li>
 * <li>job_class：{@code com.auth.service.system.schedule.task.custom.SpringInjectDemoJob}</li>
 * <li>job_params：{@code {"url":"https://httpbin.org/get"}}</li>
 * </ul>
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
