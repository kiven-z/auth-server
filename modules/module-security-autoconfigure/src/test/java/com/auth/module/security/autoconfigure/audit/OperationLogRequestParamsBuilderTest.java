package com.auth.module.security.autoconfigure.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OperationLogRequestParamsBuilder} 单元测试
 *
 * @author Bunny
 */
@DisplayName("OperationLogRequestParamsBuilder")
class OperationLogRequestParamsBuilderTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("record 为 false 时应返回 null")
	void build_shouldReturnNullWhenRecordDisabled() {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/a");
		req.setQueryString("k=v");
		assertNull(OperationLogRequestParamsBuilder.build(req, false, objectMapper));
	}

	@Test
	@DisplayName("GET 应使用 queryString 并截断超长")
	void build_shouldUseQueryStringForGet() {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/a");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < OperationLogRequestParamsBuilder.MAX_REQUEST_TEXT + 50; i++) {
			sb.append('a');
		}
		req.setQueryString(sb.toString());
		String out = OperationLogRequestParamsBuilder.build(req, true, objectMapper);
		assertEquals(OperationLogRequestParamsBuilder.MAX_REQUEST_TEXT, out.length());
	}

	@Test
	@DisplayName("非 GET 应序列化 parameterMap")
	void build_shouldSerializeParameterMapForNonGet() {
		MockHttpServletRequest req = new MockHttpServletRequest("POST", "/a");
		req.addParameter("id", "1");
		String out = OperationLogRequestParamsBuilder.build(req, true, objectMapper);
		assertTrue(out.contains("id"));
		assertTrue(out.contains("1"));
	}

}
