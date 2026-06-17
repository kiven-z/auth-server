package com.auth.module.security.contract.api.datascope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link DataScopeStorageType} 单元测试
 */
@DisplayName("DataScopeStorageType")
class DataScopeStorageTypeTest {

	@Test
	@DisplayName("parse 识别业务枚举")
	void parse_businessEnum() {
		assertEquals(DataScopeStorageType.ALL, DataScopeStorageType.parse("ALL"));
		assertEquals(DataScopeStorageType.SELF, DataScopeStorageType.parse("self"));
		assertEquals(DataScopeStorageType.DEPT, DataScopeStorageType.parse("DEPT"));
		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, DataScopeStorageType.parse("DEPT_AND_CHILD"));
	}

	@Test
	@DisplayName("parse 空值返回 null")
	void parse_blankReturnsNull() {
		assertNull(DataScopeStorageType.parse("  "));
		assertNull(DataScopeStorageType.parse(null));
	}

	@Test
	@DisplayName("parse 未知枚举名返回 null")
	void parse_unknownReturnsNull() {
		assertNull(DataScopeStorageType.parse("NODE_ONLY"));
		assertNull(DataScopeStorageType.parse("CUSTOM"));
	}

}
