package com.auth.service.system.schedule.task.builtin.http;

import com.auth.service.system.schedule.task.builtin.http.model.HttpPostJobParams;
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
 * {@link HttpPostInvokeJob} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("HttpPostInvokeJob HTTP POST 内置任务")
@ExtendWith(MockitoExtension.class)
class HttpPostInvokeJobTest {

	@Mock
	private BuiltinJobParamsParser builtinJobParamsParser;

	@Mock
	private RestTemplate restTemplate;

	private HttpPostInvokeJob httpPostInvokeJob;

	@BeforeEach
	void setUp() {
		httpPostInvokeJob = new HttpPostInvokeJob(builtinJobParamsParser, restTemplate);
	}

	@Test
	@DisplayName("execute：应使用 job_params 中的 url、headers 与 body 发起 POST")
	void execute_invokesPostWithConfiguredUrlHeadersAndBody() throws JobExecutionException {
		JobExecutionContext context = mock(JobExecutionContext.class);

		HttpPostJobParams params = new HttpPostJobParams();
		params.setVersion(1);
		params.setUrl("https://example.com/api");
		params.setHeaders(java.util.Map.of("Content-Type", "application/json"));
		params.setBody("{\"id\":1}");
		when(builtinJobParamsParser.parseFromContext(context, HttpPostJobParams.class)).thenReturn(params);
		when(restTemplate.exchange(eq("https://example.com/api"), eq(HttpMethod.POST),
				org.mockito.ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("created"));

		httpPostInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq("https://example.com/api"), eq(HttpMethod.POST), entityCaptor.capture(),
				eq(String.class));
		HttpEntity<String> request = entityCaptor.getValue();
		assertThat(request.getHeaders().getFirst("Content-Type")).isEqualTo("application/json");
		assertThat(request.getBody()).isEqualTo("{\"id\":1}");
	}

	@Test
	@DisplayName("execute：RestTemplate 异常应包装为 JobExecutionException")
	void execute_whenRestTemplateFails_wrapsJobExecutionException() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		HttpPostJobParams params = new HttpPostJobParams();
		params.setUrl("https://example.com/fail");
		when(builtinJobParamsParser.parseFromContext(context, HttpPostJobParams.class)).thenReturn(params);
		when(restTemplate.exchange(eq("https://example.com/fail"), eq(HttpMethod.POST),
				org.mockito.ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenThrow(new RuntimeException("timeout"));

		assertThatThrownBy(() -> httpPostInvokeJob.execute(context)).isInstanceOf(JobExecutionException.class)
			.hasCauseInstanceOf(RuntimeException.class);
	}

}
