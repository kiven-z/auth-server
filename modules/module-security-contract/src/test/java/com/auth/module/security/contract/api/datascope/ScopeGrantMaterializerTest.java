package com.auth.module.security.contract.api.datascope;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopeGrantMaterializerTest {

	@Test
	@DisplayName("ALL 物化为 ALL")
	void materialize_all() {
		ScopeGrant grant = ScopeGrantMaterializer.materialize(DataScopeStorageType.ALL, List.of());
		assertEquals(ScopeGrantType.ALL, grant.getScopeType());
		assertTrue(grant.getValues().isEmpty());
	}

	@Test
	@DisplayName("DEPT 物化为 NODE_ONLY")
	void materialize_dept() {
		ScopeGrant grant = ScopeGrantMaterializer.materialize(DataScopeStorageType.DEPT, List.of("1", "2"));
		assertEquals(ScopeGrantType.NODE_ONLY, grant.getScopeType());
		assertEquals(List.of("1", "2"), grant.getValues());
	}

	@Test
	@DisplayName("DEPT_AND_CHILD 物化为 CUSTOM（已展开 ID）")
	void materialize_deptAndChild() {
		ScopeGrant grant = ScopeGrantMaterializer.materialize(DataScopeStorageType.DEPT_AND_CHILD, List.of("10", "11"));
		assertEquals(ScopeGrantType.CUSTOM, grant.getScopeType());
		assertEquals(List.of("10", "11"), grant.getValues());
	}

	@Test
	@DisplayName("未知 storageType 回退 SELF")
	void materialize_nullType_selfFallback() {
		ScopeGrant grant = ScopeGrantMaterializer.materialize(null, List.of("1"));
		assertEquals(ScopeGrantType.SELF, grant.getScopeType());
	}

	@Test
	@DisplayName("parse 忽略大小写与空白")
	void parse_trimAndIgnoreCase() {
		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, DataScopeStorageType.parse(" dept_and_child "));
	}

}
