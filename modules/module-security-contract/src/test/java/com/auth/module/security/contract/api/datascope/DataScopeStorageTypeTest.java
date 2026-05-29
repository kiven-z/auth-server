package com.auth.module.security.contract.api.datascope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class DataScopeStorageTypeTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	@DisplayName("parse 识别业务枚举")
	void parse_businessEnum() {
		assertEquals(DataScopeStorageType.DEPT, DataScopeStorageType.parse("DEPT"));
	}

	@Test
	@DisplayName("parse 兼容旧运行时名 NODE_ONLY")
	void parse_legacyNodeOnly() {
		assertEquals(DataScopeStorageType.DEPT, DataScopeStorageType.parse("NODE_ONLY"));
	}

	@Test
	@DisplayName("parse 兼容旧运行时名 CUSTOM")
	void parse_legacyCustom() {
		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, DataScopeStorageType.parse("CUSTOM"));
	}

	@Test
	@DisplayName("parse 空值返回 null")
	void parse_blankReturnsNull() {
		assertNull(DataScopeStorageType.parse("  "));
	}

	@Test
	@DisplayName("Jackson 经 @JsonCreator 反序列化")
	void jsonCreator_deserializesLegacyAlias() throws Exception {
		assertEquals(DataScopeStorageType.DEPT, mapper.readValue("\"NODE_ONLY\"", DataScopeStorageType.class));
	}

}
