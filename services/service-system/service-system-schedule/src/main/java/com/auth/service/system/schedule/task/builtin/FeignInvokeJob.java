package com.auth.service.system.schedule.task.builtin;

import cn.hutool.core.net.url.UrlBuilder;
import com.auth.common.web.resttemplate.RestTemplateUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.jobparams.BuiltinRemoteHttpMethod;
import com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

/**
 * 通用微服务远程调用内置任务：通过负载均衡 {@link RestTemplate} 访问 {@code http://{serviceName}{path}}
 *
 * @author Bunny
 */
@QuartzTask(name = "FeignInvoke", description = "通用微服务调用：job_params 配置 serviceName、path、method 与可选 headers/body",
		modes = { SysJobTaskType.CUSTOM_CLASS },
		example = """
				{"version":1,"serviceName":"order-service","path":"/api/order/health","method":"GET","headers":{"X-Token":"your-token"}}
				""")
@Slf4j
@Component
public class FeignInvokeJob implements Job {

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
			// 获取jobParamsJson
			String jobParamsJson = BuiltinJobParamsParser.requireRawJson(context);
			FeignInvokeJobParams params = builtinJobParamsParser.parseAndValidate(jobParamsJson,
					FeignInvokeJobParams.class);

			// 获取serviceName和path
			String serviceName = Objects.requireNonNull(params.getServiceName(), "serviceName").trim();
			String path = Objects.requireNonNull(params.getPath(), "path").trim();
			String url = UrlBuilder.of().setScheme("http").setHost(serviceName).addPath(path).build();

			// 获取method
			BuiltinRemoteHttpMethod builtinMethod = Objects.requireNonNull(params.getMethod(), "method");
			HttpMethod method = HttpMethod.valueOf(builtinMethod.name());

			// 获取headers和body
			Map<String, String> headers = params.getHeaders();
			String body = params.getBody();
			ResponseEntity<String> response = RestTemplateUtil.exchange(loadBalancedRestTemplate, url, method, headers,
					body);

			// 获取responseBody
			String responseBody = response.getBody();
			log.debug("FeignInvokeJob ok service={} path={} method={} status={} respLen={}", params.getServiceName(),
					params.getPath(), builtinMethod, response.getStatusCode().value(),
					responseBody == null ? 0 : responseBody.length());
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

}
