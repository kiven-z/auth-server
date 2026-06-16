package com.auth.service.system.schedule.task.builtin;

import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.jobparams.BuiltinRemoteHttpMethod;
import com.auth.service.system.schedule.model.jobparams.FeignInvokeJobParams;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link FeignInvokeJob} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FeignInvokeJob 微服务远程调用内置任务")
@ExtendWith(MockitoExtension.class)
class FeignInvokeJobTest {

	@Mock
	private BuiltinJobParamsParser builtinJobParamsParser;

	@Mock
	private RestTemplate loadBalancedRestTemplate;

	private FeignInvokeJob feignInvokeJob;

	@BeforeEach
	void setUp() {
		feignInvokeJob = new FeignInvokeJob(builtinJobParamsParser, loadBalancedRestTemplate);
	}

	@Test
	@DisplayName("execute：serviceName 含首尾空格时应 trim 后拼接 URL")
	void execute_trimsServiceNameBeforeBuildingUrl() throws JobExecutionException {
		JobExecutionContext context = mock(JobExecutionContext.class);

		FeignInvokeJobParams params = new FeignInvokeJobParams();
		params.setServiceName("  auth-service  ");
		params.setPath("/api/health");
		params.setMethod(BuiltinRemoteHttpMethod.GET);
		stubJobParams(context, params);
		when(loadBalancedRestTemplate.exchange(eq("http://auth-service/api/health"), eq(HttpMethod.GET),
				ArgumentMatchers.any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("UP"));

		feignInvokeJob.execute(context);

		verify(loadBalancedRestTemplate).exchange(eq("http://auth-service/api/health"), eq(HttpMethod.GET),
				ArgumentMatchers.any(), eq(String.class));
	}

	@Test
	@DisplayName("execute：应使用负载均衡 URL 与 POST body 发起调用")
	void execute_invokesPostAgainstLoadBalancedServiceUrl() throws JobExecutionException {
		JobExecutionContext context = mock(JobExecutionContext.class);

		FeignInvokeJobParams params = new FeignInvokeJobParams();
		params.setVersion(1);
		params.setServiceName("order-service");
		params.setPath("/api/internal/sync");
		params.setMethod(BuiltinRemoteHttpMethod.POST);
		params.setHeaders(java.util.Map.of("Content-Type", "application/json"));
		params.setBody("{\"id\":1}");
		stubJobParams(context, params);
		when(loadBalancedRestTemplate.exchange(eq("http://order-service/api/internal/sync"), eq(HttpMethod.POST),
				ArgumentMatchers.any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("ok"));

		feignInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(loadBalancedRestTemplate).exchange(eq("http://order-service/api/internal/sync"), eq(HttpMethod.POST),
				entityCaptor.capture(), eq(String.class));
		HttpEntity<String> request = entityCaptor.getValue();
		assertThat(request.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
		assertThat(request.getBody()).isEqualTo("{\"id\":1}");
	}

	@Test
	@DisplayName("execute：GET 调用不应携带 body")
	void execute_getRequest_usesHeadersOnlyEntity() throws JobExecutionException {
		JobExecutionContext context = mock(JobExecutionContext.class);

		FeignInvokeJobParams params = new FeignInvokeJobParams();
		params.setServiceName("auth-service");
		params.setPath("api/health");
		params.setMethod(BuiltinRemoteHttpMethod.GET);
		stubJobParams(context, params);
		when(loadBalancedRestTemplate.exchange(eq("http://auth-service/api/health"), eq(HttpMethod.GET),
				ArgumentMatchers.any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("UP"));

		feignInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<Void>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(loadBalancedRestTemplate).exchange(eq("http://auth-service/api/health"), eq(HttpMethod.GET),
				entityCaptor.capture(), eq(String.class));
		assertThat(entityCaptor.getValue().getBody()).isNull();
	}

	@Test
	@DisplayName("execute：RestTemplate 异常应包装为 JobExecutionException")
	void execute_whenRestTemplateFails_wrapsJobExecutionException() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		FeignInvokeJobParams params = new FeignInvokeJobParams();
		params.setServiceName("order-service");
		params.setPath("/api/fail");
		params.setMethod(BuiltinRemoteHttpMethod.DELETE);
		stubJobParams(context, params);
		when(loadBalancedRestTemplate.exchange(eq("http://order-service/api/fail"), eq(HttpMethod.DELETE),
				ArgumentMatchers.any(), eq(String.class)))
			.thenThrow(new RuntimeException("connection refused"));

		assertThatThrownBy(() -> feignInvokeJob.execute(context)).isInstanceOf(JobExecutionException.class)
			.hasCauseInstanceOf(RuntimeException.class);
	}

	private void stubJobParams(JobExecutionContext context, FeignInvokeJobParams params) {
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(SysJobQuartzDataKeys.JOB_PARAMS, "{}");
		when(context.getMergedJobDataMap()).thenReturn(jobDataMap);
		when(builtinJobParamsParser.parseAndValidate(anyString(), eq(FeignInvokeJobParams.class))).thenReturn(params);
	}

}
