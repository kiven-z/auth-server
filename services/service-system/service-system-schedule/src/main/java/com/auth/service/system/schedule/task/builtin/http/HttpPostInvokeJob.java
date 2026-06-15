package com.auth.service.system.schedule.task.builtin.http;

import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.builtin.BuiltinQuartzJob;
import com.auth.service.system.schedule.task.builtin.feign.support.FeignInvokeSupport;
import com.auth.service.system.schedule.task.builtin.http.model.HttpPostJobParams;
import com.auth.service.system.schedule.task.builtin.support.BuiltinJobParamsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

/**
 * 通用 HTTP POST 内置任务：从 {@code job_params} 读取 URL、可选请求头与请求体并发起 POST <pre>
 * {
 *   "version": 1,
 *   "url": "https://api.example.com/orders",
 *   "headers": { "Content-Type": "application/json" },
 *   "body": "{\"id\":1}"
 * }
 * </pre>
 *
 * @author Bunny
 */
@QuartzTask(name = "HttpPostInvoke", description = "通用 HTTP POST：job_params 配置 url、可选 headers 与 body",
		modes = { SysJobTaskType.CUSTOM_CLASS })
@RequiredArgsConstructor
@Slf4j
@Component
public class HttpPostInvokeJob implements BuiltinQuartzJob {

	private final BuiltinJobParamsParser builtinJobParamsParser;

	private final RestTemplate restTemplate;

	/**
	 * 将参数 Map 转为 HTTP 请求头
	 * @param headers 请求头 Map
	 * @return HttpHeaders
	 */
	private static HttpHeaders toHttpHeaders(Map<String, String> headers) {
		return FeignInvokeSupport.toHttpHeaders(headers);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			HttpPostJobParams params = builtinJobParamsParser.parseFromContext(context, HttpPostJobParams.class);
			String url = Objects.requireNonNull(params.getUrl(), "url");
			HttpHeaders headers = toHttpHeaders(params.getHeaders());
			HttpEntity<String> request = new HttpEntity<>(params.getBody(), headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
			String responseBody = response.getBody();
			log.debug("HttpPostInvokeJob ok url={} status={} respLen={}", url, response.getStatusCode().value(),
					responseBody == null ? 0 : responseBody.length());
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

}
