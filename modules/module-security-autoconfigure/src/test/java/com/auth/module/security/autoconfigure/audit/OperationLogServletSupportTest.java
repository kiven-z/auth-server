package com.auth.module.security.autoconfigure.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * {@link OperationLogServletSupport} 单元测试
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class OperationLogServletSupportTest {

	@Mock
	private HttpServletRequest request;

	@Test
	@DisplayName("truncate 应在超长时截断")
	void truncate_shouldClip() {
		// 与 log_operation 文本列上限策略一致
		String s = "a".repeat(OperationLogServletSupport.MAX_REQUEST_TEXT + 10);
		assertEquals(OperationLogServletSupport.MAX_REQUEST_TEXT,
				OperationLogServletSupport.truncate(s, OperationLogServletSupport.MAX_REQUEST_TEXT).length());
	}

	@Test
	@DisplayName("resolveClientIp 应优先解析 X-Forwarded-For 首段")
	void resolveClientIp_shouldPreferXff() {
		when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 10.0.0.1");
		assertEquals("203.0.113.1", OperationLogServletSupport.resolveClientIp(request));
	}

	@Test
	@DisplayName("buildRequestParams 在 GET 下应返回 query string")
	void buildRequestParams_get_shouldUseQueryString() throws Exception {
		when(request.getMethod()).thenReturn("GET");
		when(request.getQueryString()).thenReturn("id=1");
		assertEquals("id=1", OperationLogServletSupport.buildRequestParams(request, true, new ObjectMapper()));
	}

	@Test
	@DisplayName("buildRequestParams 在关闭记录时应返回 null")
	void buildRequestParams_whenDisabled_shouldReturnNull() throws Exception {
		assertNull(OperationLogServletSupport.buildRequestParams(request, false, new ObjectMapper()));
	}

	@Test
	@DisplayName("buildRequestParams 在 POST 下应序列化 parameterMap")
	void buildRequestParams_post_shouldSerializeMap() throws Exception {
		when(request.getMethod()).thenReturn("POST");
		when(request.getParameterMap()).thenReturn(Map.of("k", new String[] { "v" }));
		String s = OperationLogServletSupport.buildRequestParams(request, true, new ObjectMapper());
		assertEquals("{\"k\":[\"v\"]}", s);
	}

	@Test
	@DisplayName("readRequestBody 在普通请求上应返回 null")
	void readRequestBody_plainRequest_shouldReturnNull() {
		MockHttpServletRequest plain = new MockHttpServletRequest();
		assertNull(OperationLogServletSupport.readRequestBody(plain, true));
	}

}
