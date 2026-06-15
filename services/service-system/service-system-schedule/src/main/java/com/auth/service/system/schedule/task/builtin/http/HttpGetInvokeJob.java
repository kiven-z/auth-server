package com.auth.service.system.schedule.task.builtin.http;

import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.builtin.BuiltinQuartzJob;
import com.auth.service.system.schedule.task.builtin.feign.support.FeignInvokeSupport;
import com.auth.service.system.schedule.task.builtin.http.model.HttpGetJobParams;
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
 * 通用 HTTP GET 内置任务：从 {@code job_params} 读取 URL 与可选请求头并发起 GET <pre>
 * {
 *   "version": 1,
 *   "url": "https://example.com/api/health",
 *   "headers": {
 *     "X-Token": "your-token"
 *   }
 * }
 * </pre>
 *
 * @author Bunny
 */
@QuartzTask(name = "HttpGetInvoke", description = "通用 HTTP GET：job_params 配置 url 与可选 headers",
		modes = { SysJobTaskType.CUSTOM_CLASS })
@RequiredArgsConstructor
@Slf4j
@Component
public class HttpGetInvokeJob implements BuiltinQuartzJob {

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
			HttpGetJobParams params = builtinJobParamsParser.parseFromContext(context, HttpGetJobParams.class);
			String url = Objects.requireNonNull(params.getUrl(), "url");
			HttpHeaders headers = toHttpHeaders(params.getHeaders());
			HttpEntity<Void> request = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
			String body = response.getBody();
			log.debug("HttpGetInvokeJob ok url={} status={} respLen={}", url, response.getStatusCode().value(),
					body == null ? 0 : body.length());
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

}
