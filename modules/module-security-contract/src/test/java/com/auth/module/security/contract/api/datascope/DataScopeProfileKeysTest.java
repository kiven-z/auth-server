package com.auth.module.security.contract.api.datascope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataScopeProfileKeysTest {

	@Test
	@DisplayName("默认资源键映射为 DEPT::default")
	void defaultResource_shouldMapToDefaultProfileKey() {
		assertEquals("DEPT::default", DataScopeProfileKeys.forResource("default"));
		assertEquals("DEPT::default", DataScopeProfileKeys.forResource(null));
		assertEquals("DEPT::default", DataScopeProfileKeys.forResource(""));
	}

	@Test
	@DisplayName("自定义资源键生成 DEPT::resource")
	void customResource_shouldComposeProfileKey() {
		assertEquals("DEPT::order", DataScopeProfileKeys.forResource("order"));
	}

}
