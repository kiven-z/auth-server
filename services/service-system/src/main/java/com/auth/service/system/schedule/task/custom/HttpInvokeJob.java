package com.auth.service.system.schedule.task.custom;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.auth.service.system.common.exception.SystemBusinessException;

import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.HTTP_JOB_INVALID_HTTP_METHOD;
import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.HTTP_JOB_MISSING_URL;
import static com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode.HTTP_JOB_NON_2XX_RESPONSE;
import com.auth.service.system.schedule.annotation.QuartzTask;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 内置 HTTP 调用 Job：从 {@code JobDataMap} 读取 url/method/headers/body 并发起请求
 *
 * <p>
 * 参数约定（写入 {@code sys_job.job_params} JSON 后会被合并进 JobDataMap）：
 * </p>
 * <ul>
 * <li>url: string (必填)</li>
 * <li>method: string (可选，默认 GET)</li>
 * <li>headers: object (可选)</li>
 * <li>body: any (可选)</li>
 * </ul>
 *
 * @author Bunny
 */
@QuartzTask(name = "HttpInvokeJob", description = "HTTP 调用任务（job_params: url/method/headers/body）")
@Slf4j
@Component
public class HttpInvokeJob implements Job {

	private static String asString(Object v) {
		return v == null ? null : String.valueOf(v);
	}

	private static RestTemplate getRestTemplate(JobExecutionContext context) {
		try {
			Object app = context.getScheduler().getContext().get("applicationContext");
			if (app instanceof org.springframework.context.ApplicationContext ac) {
				return ac.getBean(RestTemplate.class);
			}
		}
		catch (Exception exception) {
			log.warn("HttpInvokeJob getRestTemplate failed", exception);
		}
		// 最小兜底：没有 RestTemplate Bean 时直接 new（不走 Spring 配置）
		return new RestTemplate();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String[] keys = context.getMergedJobDataMap().getKeys();
		Map<String, Object> map = new HashMap<>(keys.length);
		for (String k : keys) {
			map.put(k, context.getMergedJobDataMap().get(k));
		}
		String url = asString(map.get("url"));
		if (CharSequenceUtil.isBlank(url)) {
			throw new JobExecutionException(new SystemBusinessException(HTTP_JOB_MISSING_URL));
		}
		url = java.util.Objects.requireNonNull(url, "url").trim();
		String methodRaw = asString(map.get("method"));
		if (CharSequenceUtil.isBlank(methodRaw)) {
			methodRaw = "GET";
		}
		methodRaw = java.util.Objects.requireNonNull(methodRaw, "method").trim();
		HttpMethod method;
		try {
			method = HttpMethod.valueOf(methodRaw.toUpperCase());
		}
		catch (Exception e) {
			throw new JobExecutionException(new SystemBusinessException(HTTP_JOB_INVALID_HTTP_METHOD, methodRaw));
		}

		HttpHeaders headers = new HttpHeaders();
		Object headersObj = map.get("headers");
		if (headersObj instanceof Map<?, ?> hm) {
			for (Map.Entry<?, ?> e : hm.entrySet()) {
				if (e.getKey() != null && e.getValue() != null) {
					headers.add(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
				}
			}
		}
		else if (headersObj instanceof String s && CharSequenceUtil.isNotBlank(s)) {
			try {
				JSONObject jo = JSONUtil.parseObj(s);
				for (String key : jo.keySet()) {
					Object v = jo.get(key);
					if (v != null) {
						headers.add(key, String.valueOf(v));
					}
				}
			}
			catch (Exception exception) {
				log.warn("HttpInvokeJob parse headers failed", exception);
			}
		}

		Object body = map.get("body");
		HttpEntity<?> entity = (body == null) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);

		long start = System.currentTimeMillis();
		try {
			RestTemplate restTemplate = getRestTemplate(context);
			ResponseEntity<String> resp = restTemplate.exchange(url, method, entity, String.class);
			long elapsed = System.currentTimeMillis() - start;
			int code = resp.getStatusCode().value();
			log.info("HttpInvokeJob ok url={} method={} status={} elapsedMs={}", url, method, code, elapsed);
			if (!resp.getStatusCode().is2xxSuccessful()) {
				throw new SystemBusinessException(HTTP_JOB_NON_2XX_RESPONSE, String.valueOf(code));
			}
		}
		catch (Exception e) {
			long elapsed = System.currentTimeMillis() - start;
			log.warn("HttpInvokeJob failed url={} method={} elapsedMs={} err={}", url, method, elapsed, e.getMessage());
			throw new JobExecutionException(e);
		}
	}

}
