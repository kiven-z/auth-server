package com.auth.service.system.schedule.support.builtin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BuiltinHttpSupport} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("BuiltinHttpSupport HTTP 请求头组装")
class BuiltinHttpSupportTest {

	@Test
	@DisplayName("toHttpHeaders：空 Map 应返回空请求头")
	void toHttpHeaders_whenEmpty_returnsEmptyHeaders() {
		HttpHeaders headers = BuiltinHttpSupport.toHttpHeaders(Map.of());

		assertThat(headers).isEmpty();
	}

	@Test
	@DisplayName("toHttpHeaders：应复制 Map 中的键值")
	void toHttpHeaders_whenPresent_copiesEntries() {
		HttpHeaders headers = BuiltinHttpSupport.toHttpHeaders(Map.of("X-Token", "abc"));

		assertThat(headers.getFirst("X-Token")).isEqualTo("abc");
	}

	@Test
	@DisplayName("buildUrlWithQuery：无 query 时应返回原 URL")
	void buildUrlWithQuery_whenEmpty_returnsOriginalUrl() {
		assertThat(BuiltinHttpSupport.buildUrlWithQuery("https://example.com/api", Map.of()))
			.isEqualTo("https://example.com/api");
	}

	@Test
	@DisplayName("buildUrlWithQuery：应拼接查询参数")
	void buildUrlWithQuery_whenPresent_appendsQuery() {
		String url = BuiltinHttpSupport.buildUrlWithQuery("https://example.com/api", Map.of("page", "1", "size", "10"));

		assertThat(url).contains("page=1").contains("size=10");
	}

}
