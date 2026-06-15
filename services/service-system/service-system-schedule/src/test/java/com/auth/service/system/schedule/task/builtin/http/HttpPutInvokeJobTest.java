package com.auth.service.system.schedule.task.builtin.http;

import com.auth.service.system.schedule.task.builtin.http.model.HttpPutJobParams;
import com.auth.service.system.schedule.task.builtin.support.BuiltinJobParamsParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link HttpPutInvokeJob} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("HttpPutInvokeJob HTTP PUT 内置任务")
@ExtendWith(MockitoExtension.class)
class HttpPutInvokeJobTest {

	@Mock
	private BuiltinJobParamsParser builtinJobParamsParser;

	@Mock
	private RestTemplate restTemplate;

	private HttpPutInvokeJob httpPutInvokeJob;

	@BeforeEach
	void setUp() {
		httpPutInvokeJob = new HttpPutInvokeJob(builtinJobParamsParser, restTemplate);
	}

	@Test
	@DisplayName("execute：应使用 job_params 中的 url、headers 与 body 发起 PUT")
	void execute_invokesPutWithConfiguredUrlHeadersAndBody() throws JobExecutionException {
		JobExecutionContext context = mock(JobExecutionContext.class);

		HttpPutJobParams params = new HttpPutJobParams();
		params.setVersion(1);
		params.setUrl("https://example.com/api/1");
		params.setHeaders(java.util.Map.of("Content-Type", "application/json"));
		params.setBody("{\"status\":\"active\"}");
		when(builtinJobParamsParser.parseFromContext(context, HttpPutJobParams.class)).thenReturn(params);
		when(restTemplate.exchange(eq("https://example.com/api/1"), eq(HttpMethod.PUT),
				org.mockito.ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("updated"));

		httpPutInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq("https://example.com/api/1"), eq(HttpMethod.PUT), entityCaptor.capture(),
				eq(String.class));
		HttpEntity<String> request = entityCaptor.getValue();
		assertThat(request.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
		assertThat(request.getBody()).isEqualTo("{\"status\":\"active\"}");
	}

	@Test
	@DisplayName("execute：RestTemplate 异常应包装为 JobExecutionException")
	void execute_whenRestTemplateFails_wrapsJobExecutionException() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		HttpPutJobParams params = new HttpPutJobParams();
		params.setUrl("https://example.com/fail");
		when(builtinJobParamsParser.parseFromContext(context, HttpPutJobParams.class)).thenReturn(params);
		when(restTemplate.exchange(eq("https://example.com/fail"), eq(HttpMethod.PUT),
				org.mockito.ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenThrow(new RuntimeException("timeout"));

		assertThatThrownBy(() -> httpPutInvokeJob.execute(context)).isInstanceOf(JobExecutionException.class)
			.hasCauseInstanceOf(RuntimeException.class);
	}

}
