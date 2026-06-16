package com.auth.service.system.schedule.task.builtin;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.url.UrlBuilder;
import com.auth.common.web.resttemplate.RestTemplateUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.model.jobparams.HttpInvokeJobParams;
import com.auth.service.system.schedule.task.support.BuiltinJobParamsParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
				{"version":1,"method":"POST","url":"https://example.com/api/items","headers":{"Content-Type":"application/json"},"query": { "userId": 2 },"body":{"id":1}}
				""")
@RequiredArgsConstructor
@Slf4j
@Component
public class HttpInvokeJob implements Job {

	private final BuiltinJobParamsParser builtinJobParamsParser;

	private final RestTemplate restTemplate;

	private final ObjectMapper objectMapper;

	/**
	 * 将查询参数拼接到 URL
	 * @param url 原始 URL
	 * @param query 查询参数
	 * @return 拼接后的 URL
	 */
	static String buildUrlWithQuery(String url, Map<String, String> query) {
		if (MapUtil.isEmpty(query)) {
			return url;
		}

		UrlBuilder builder = UrlBuilder.ofHttp(url);
		query.forEach(builder::addQuery);
		return builder.build();
	}

	/**
	 * 将 JSON 请求体序列化为 HTTP 原始文本
	 * @param body JSON 节点
	 * @param objectMapper JSON 序列化器
	 * @return 序列化后的文本；{@code null} 或 JSON null 时返回 {@code null}
	 */
	static String serializeBody(JsonNode body, ObjectMapper objectMapper) throws JsonProcessingException {
		if (body == null || body.isNull()) {
			return null;
		}
		return objectMapper.writeValueAsString(body);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			// 读取并校验 job_params
			String jobParamsJson = BuiltinJobParamsParser.requireRawJson(context);
			HttpInvokeJobParams params = builtinJobParamsParser.parseAndValidate(jobParamsJson,
					HttpInvokeJobParams.class);

			// 拼接 URL 与 query 参数
			String paramsUrl = params.getUrl();
			Map<String, String> query = params.getQuery();
			String url = buildUrlWithQuery(Objects.requireNonNull(paramsUrl, "url"), query);

			HttpMethod method = Objects.requireNonNull(params.getMethod(), "method");

			// 发起 HTTP 调用
			Map<String, String> headers = params.getHeaders();
			String body = serializeBody(params.getBody(), objectMapper);
			ResponseEntity<String> response = RestTemplateUtil.exchange(restTemplate, url, method, headers, body);

			// 记录响应摘要
			String responseBody = response.getBody();
			log.debug("HttpInvokeJob ok method={} url={} status={} respLen={}", method, url,
					response.getStatusCode().value(), responseBody == null ? 0 : responseBody.length());
		}
		catch (Exception exception) {
			throw new JobExecutionException(exception);
		}
	}

}
