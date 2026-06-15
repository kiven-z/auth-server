package com.auth.service.system.schedule.task.builtin.feign;

import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.builtin.BuiltinQuartzJob;
import com.auth.service.system.schedule.task.builtin.feign.model.BuiltinRemoteHttpMethod;
import com.auth.service.system.schedule.task.builtin.feign.model.FeignInvokeJobParams;
import com.auth.service.system.schedule.task.builtin.feign.support.FeignInvokeSupport;
import com.auth.service.system.schedule.task.builtin.support.BuiltinJobParamsParser;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * 通用微服务远程调用内置任务：通过负载均衡 {@link RestTemplate} 访问 {@code http://{serviceName}{path}}
 *
 * @author Bunny
 */
@QuartzTask(name = "FeignInvoke", description = "通用微服务调用：job_params 配置 serviceName、path、method 与可选 headers/body",
		modes = { SysJobTaskType.CUSTOM_CLASS })
@Slf4j
@Component
public class FeignInvokeJob implements BuiltinQuartzJob {

	private final BuiltinJobParamsParser builtinJobParamsParser;

	private final RestTemplate loadBalancedRestTemplate;

	public FeignInvokeJob(BuiltinJobParamsParser builtinJobParamsParser,
			@Qualifier("loadBalancedRestTemplate") RestTemplate loadBalancedRestTemplate) {
		this.builtinJobParamsParser = builtinJobParamsParser;
		this.loadBalancedRestTemplate = loadBalancedRestTemplate;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			FeignInvokeJobParams params = builtinJobParamsParser.parseFromContext(context, FeignInvokeJobParams.class);
			String url = FeignInvokeSupport.buildServiceUrl(
					Objects.requireNonNull(params.getServiceName(), "serviceName"),
					Objects.requireNonNull(params.getPath(), "path"));
			HttpHeaders headers = FeignInvokeSupport.toHttpHeaders(params.getHeaders());
			BuiltinRemoteHttpMethod method = Objects.requireNonNull(params.getMethod(), "method");
			ResponseEntity<String> response = exchange(url, method, headers, params.getBody());
			String responseBody = response.getBody();
			log.debug("FeignInvokeJob ok service={} path={} method={} status={} respLen={}", params.getServiceName(),
					params.getPath(), method, response.getStatusCode().value(),
					responseBody == null ? 0 : responseBody.length());
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

	/**
	 * 按 HTTP 方法发起远程调用
	 * @param url 负载均衡 URL
	 * @param method 内置 HTTP 方法
	 * @param headers 请求头
	 * @param body 请求体（GET/DELETE 忽略）
	 * @return 响应
	 */
	private ResponseEntity<String> exchange(String url, BuiltinRemoteHttpMethod method, HttpHeaders headers,
			String body) {
		return switch (method) {
			case GET -> loadBalancedRestTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
			case DELETE ->
				loadBalancedRestTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
			case POST ->
				loadBalancedRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
			case PUT ->
				loadBalancedRestTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
		};
	}

}
