package com.auth.service.system.schedule.task.builtin.http;

import com.auth.service.system.schedule.task.builtin.http.model.HttpGetJobParams;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HttpGetInvokeJob} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("HttpGetInvokeJob HTTP GET 内置任务")
@ExtendWith(MockitoExtension.class)
class HttpGetInvokeJobTest {

	@Mock
	private BuiltinJobParamsParser builtinJobParamsParser;

	@Mock
	private RestTemplate restTemplate;

	private HttpGetInvokeJob httpGetInvokeJob;

	@BeforeEach
	void setUp() {
		httpGetInvokeJob = new HttpGetInvokeJob(builtinJobParamsParser, restTemplate);
	}

	@Test
	@DisplayName("execute：应使用 job_params 中的 url 与 headers 发起 GET")
	void execute_invokesGetWithConfiguredUrlAndHeaders() throws JobExecutionException {
		JobExecutionContext context = mock(JobExecutionContext.class);

		HttpGetJobParams params = new HttpGetJobParams();
		params.setVersion(1);
		params.setUrl("https://example.com/api");
		params.setHeaders(java.util.Map.of("X-Token", "abc"));
		when(builtinJobParamsParser.parseFromContext(context, HttpGetJobParams.class)).thenReturn(params);
		when(restTemplate.exchange(eq("https://example.com/api"), eq(HttpMethod.GET),
				org.mockito.ArgumentMatchers.<HttpEntity<Void>>any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("ok"));

		httpGetInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<Void>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq("https://example.com/api"), eq(HttpMethod.GET), entityCaptor.capture(),
				eq(String.class));
		assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Token")).isEqualTo("abc");
	}

	@Test
	@DisplayName("execute：RestTemplate 异常应包装为 JobExecutionException")
	void execute_whenRestTemplateFails_wrapsJobExecutionException() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		HttpGetJobParams params = new HttpGetJobParams();
		params.setUrl("https://example.com/fail");
		when(builtinJobParamsParser.parseFromContext(context, HttpGetJobParams.class)).thenReturn(params);
		when(restTemplate.exchange(eq("https://example.com/fail"), eq(HttpMethod.GET),
				org.mockito.ArgumentMatchers.<HttpEntity<Void>>any(), eq(String.class)))
			.thenThrow(new RuntimeException("timeout"));

		assertThatThrownBy(() -> httpGetInvokeJob.execute(context)).isInstanceOf(JobExecutionException.class)
			.hasCauseInstanceOf(RuntimeException.class);
	}

}
