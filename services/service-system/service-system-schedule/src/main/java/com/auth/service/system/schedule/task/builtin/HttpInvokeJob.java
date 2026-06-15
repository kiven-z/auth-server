package com.auth.service.system.schedule.task.builtin;

import cn.hutool.core.net.url.UrlBuilder;
import com.auth.common.web.resttemplate.RestTemplateUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.support.task.BuiltinJobParamsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

/**
 * 通用 HTTP 内置任务：从 {@code job_params} 读取 method、url 与可选 headers/query/body 并发起调用
 *
 * @author Bunny
 */
@QuartzTask(name = "HttpInvoke", description = "通用 HTTP 调用：job_params 配置 method、url 与可选 headers/query/body",
		modes = { SysJobTaskType.CUSTOM_CLASS },
		example = """
				{"version":1,"method":"GET","url":"https://example.com/api/health","headers":{"X-Token":"your-token"},"query":{"page":"1"}}
				""")
@RequiredArgsConstructor
@Slf4j
@Component
public class HttpInvokeJob implements BuiltinQuartzJob {

	private final BuiltinJobParamsParser builtinJobParamsParser;

	private final RestTemplate restTemplate;

	/**
	 * 将查询参数拼接到 URL
	 * @param url 原始 URL
	 * @param query 查询参数
	 * @return 拼接后的 URL
	 */
	static @NonNull String buildUrlWithQuery(@NonNull String url, @Nullable Map<String, String> query) {
		if (query == null || query.isEmpty()) {
			return url;
		}
		UrlBuilder builder = UrlBuilder.ofHttp(url);
		query.forEach(builder::addQuery);
		return builder.build();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			HttpInvokeJobParams params = builtinJobParamsParser
				.parseAndValidate(BuiltinJobParamsParser.requireRawJson(context), HttpInvokeJobParams.class);
			String url = buildUrlWithQuery(Objects.requireNonNull(params.getUrl(), "url"), params.getQuery());

			HttpMethod method = HttpMethod.valueOf(Objects.requireNonNull(params.getMethod(), "method").name());

			ResponseEntity<String> response = RestTemplateUtil.exchange(restTemplate, url, method, params.getHeaders(),
					params.getBody());
			String body = response.getBody();
			log.debug("HttpInvokeJob ok method={} url={} status={} respLen={}", method, url,
					response.getStatusCode().value(), body == null ? 0 : body.length());
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

}
