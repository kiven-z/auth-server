package com.auth.module.security.contract.api.datascope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link DataScopeStorageType} 单元测试
 */
@DisplayName("DataScopeStorageType")
class DataScopeStorageTypeTest {

	@Test
	@DisplayName("parse 识别业务枚举")
	void parse_businessEnum() {
		assertEquals(DataScopeStorageType.DEPT, DataScopeStorageType.parse("DEPT"));
		assertEquals(DataScopeStorageType.CUSTOMER, DataScopeStorageType.parse("CUSTOMER"));
	}

	@Test
	@DisplayName("parse 空值返回 null")
	void parse_blankReturnsNull() {
		assertNull(DataScopeStorageType.parse("  "));
		assertNull(DataScopeStorageType.parse(null));
	}

}
