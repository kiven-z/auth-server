package com.auth.module.security.contract.api.datascope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataScopeKeyUtilTest {

	@Test
	@DisplayName("组合键格式为 DIMENSION::RESOURCE")
	void compose_formatsKey() {
		assertEquals("DEPT::default", DataScopeKeyUtil.compose(DataScopeDimension.DEPT, DataScopeResource.DEFAULT));
		assertEquals("DEPT::order", DataScopeKeyUtil.compose("dept", "order"));
	}

	@Test
	@DisplayName("维度默认画像键")
	void deptDefaultProfileKey() {
		assertEquals("DEPT::default", DataScopeDimension.DEPT.defaultProfileKey());
	}

	@Test
	@DisplayName("空白维度或资源键抛出异常")
	void normalize_blank_throws() {
		assertThrows(IllegalArgumentException.class, () -> DataScopeKeyUtil.normalizeDimension(" "));
		assertThrows(IllegalArgumentException.class, () -> DataScopeKeyUtil.normalizeResourceCode(null));
	}

}
