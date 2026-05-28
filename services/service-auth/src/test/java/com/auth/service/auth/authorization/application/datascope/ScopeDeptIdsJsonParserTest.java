package com.auth.service.auth.authorization.application.datascope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopeDeptIdsJsonParserTest {

	@Test
	@DisplayName("解析 JSON 数组")
	void parse_jsonArray() {
		assertEquals(List.of("1", "2", "3"), ScopeDeptIdsJsonParser.parse("[1, 2, 3]"));
	}

	@Test
	@DisplayName("空字符串返回空列表")
	void parse_blank_returnsEmpty() {
		assertTrue(ScopeDeptIdsJsonParser.parse("").isEmpty());
		assertTrue(ScopeDeptIdsJsonParser.parse(null).isEmpty());
	}

}
