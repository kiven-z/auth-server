package com.auth.service.system.schedule.task.builtin.http;

import com.auth.service.system.schedule.task.builtin.http.model.HttpDeleteJobParams;
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
 * {@link HttpDeleteInvokeJob} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("HttpDeleteInvokeJob HTTP DELETE 内置任务")
@ExtendWith(MockitoExtension.class)
class HttpDeleteInvokeJobTest {

	@Mock
	private BuiltinJobParamsParser builtinJobParamsParser;

	@Mock
	private RestTemplate restTemplate;

	private HttpDeleteInvokeJob httpDeleteInvokeJob;

	@BeforeEach
	void setUp() {
		httpDeleteInvokeJob = new HttpDeleteInvokeJob(builtinJobParamsParser, restTemplate);
	}

	@Test
	@DisplayName("execute：应使用 job_params 中的 url 与 headers 发起 DELETE")
	void execute_invokesDeleteWithConfiguredUrlAndHeaders() throws JobExecutionException {
		JobExecutionContext context = mock(JobExecutionContext.class);

		HttpDeleteJobParams params = new HttpDeleteJobParams();
		params.setVersion(1);
		params.setUrl("https://example.com/api/1");
		params.setHeaders(java.util.Map.of("X-Token", "abc"));
		when(builtinJobParamsParser.parseFromContext(context, HttpDeleteJobParams.class)).thenReturn(params);
		when(restTemplate.exchange(eq("https://example.com/api/1"), eq(HttpMethod.DELETE),
				org.mockito.ArgumentMatchers.<HttpEntity<Void>>any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("deleted"));

		httpDeleteInvokeJob.execute(context);

		ArgumentCaptor<HttpEntity<Void>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq("https://example.com/api/1"), eq(HttpMethod.DELETE), entityCaptor.capture(),
				eq(String.class));
		assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Token")).isEqualTo("abc");
	}

	@Test
	@DisplayName("execute：RestTemplate 异常应包装为 JobExecutionException")
	void execute_whenRestTemplateFails_wrapsJobExecutionException() {
		JobExecutionContext context = mock(JobExecutionContext.class);
		HttpDeleteJobParams params = new HttpDeleteJobParams();
		params.setUrl("https://example.com/fail");
		when(builtinJobParamsParser.parseFromContext(context, HttpDeleteJobParams.class)).thenReturn(params);
		when(restTemplate.exchange(eq("https://example.com/fail"), eq(HttpMethod.DELETE),
				org.mockito.ArgumentMatchers.<HttpEntity<Void>>any(), eq(String.class)))
			.thenThrow(new RuntimeException("timeout"));

		assertThatThrownBy(() -> httpDeleteInvokeJob.execute(context)).isInstanceOf(JobExecutionException.class)
			.hasCauseInstanceOf(RuntimeException.class);
	}

}
