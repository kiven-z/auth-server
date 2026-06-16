package com.auth.service.system.schedule.task.builtin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link HttpInvokeJob} URL 组装单元测试。
 *
 * @author Bunny
 */
@DisplayName("HttpInvokeJob URL 查询参数拼接")
class HttpInvokeJobUrlTest {

	@Test
	@DisplayName("buildUrlWithQuery：无 query 时返回原 URL")
	void buildUrlWithQuery_whenQueryEmpty_returnsOriginalUrl() {
		assertThat(HttpInvokeJob.buildUrlWithQuery("https://example.com/api", null))
			.isEqualTo("https://example.com/api");
	}

	@Test
	@DisplayName("buildUrlWithQuery：应拼接 query 参数")
	void buildUrlWithQuery_whenQueryPresent_appendsParams() {
		assertThat(HttpInvokeJob.buildUrlWithQuery("https://example.com/api", Map.of("page", "1"))).contains("page=1");
	}

	@Test
	@DisplayName("serializeBody：JSON 对象应序列化为 HTTP 文本")
	void serializeBody_whenJsonObject_returnsJsonText() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String actual = HttpInvokeJob.serializeBody(objectMapper.readTree("{\"title\":\"foo\",\"userId\":1}"),
				objectMapper);

		assertThat(actual).isEqualTo("{\"title\":\"foo\",\"userId\":1}");
	}

	@Test
	@DisplayName("serializeBody：null 或 JSON null 应返回 null")
	void serializeBody_whenNull_returnsNull() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();

		assertThat(HttpInvokeJob.serializeBody(null, objectMapper)).isNull();
		assertThat(HttpInvokeJob.serializeBody(objectMapper.nullNode(), objectMapper)).isNull();
	}

}
