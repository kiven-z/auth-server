package com.auth.service.system.schedule.task.builtin;

import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.jobparams.BuiltinRemoteHttpMethod;
import com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams;
import com.auth.service.system.schedule.support.builtin.BuiltinJobParamsParser;
import com.auth.service.system.schedule.support.builtin.FeignInvokeSupport;
import com.auth.service.system.schedule.support.builtin.HttpInvokeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Qualifier;
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
			BuiltinRemoteHttpMethod builtinMethod = Objects.requireNonNull(params.getMethod(), "method");
			HttpMethod method = HttpMethod.valueOf(builtinMethod.name());
			ResponseEntity<String> response = HttpInvokeExecutor.exchange(loadBalancedRestTemplate, url, method,
					params.getHeaders(), params.getBody());
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
