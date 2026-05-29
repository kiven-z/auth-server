package com.auth.module.security.contract.api.authorization;

import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.contract.api.granttable.ScopeGrantEffectiveMerger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScopeGrantEffectiveMergerTest {

	@Test
	@DisplayName("ALL 覆盖其它类型")
	void mergeLoose_allWins() {
		ScopeGrant merged = ScopeGrantEffectiveMerger
			.mergeLoose(List.of(ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build(),
					ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build()));
		assertNotNull(merged);
		assertEquals(DataScopeStorageType.ALL, merged.getScopeType());
	}

	@Test
	@DisplayName("DEPT 与 DEPT_AND_CHILD 合并为 DEPT_AND_CHILD 并集")
	void mergeLoose_deptAndChildWinsType() {
		ScopeGrant merged = ScopeGrantEffectiveMerger.mergeLoose(List.of(
				ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(1L, 2L)).build(),
				ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT_AND_CHILD).values(List.of(2L, 3L)).build()));
		assertNotNull(merged);
		assertEquals(DataScopeStorageType.DEPT_AND_CHILD, merged.getScopeType());
		assertEquals(List.of(1L, 2L, 3L), merged.getValues());
	}

	@Test
	@DisplayName("仅 DEPT 合并后仍为 DEPT")
	void mergeLoose_onlyDeptStaysDept() {
		ScopeGrant merged = ScopeGrantEffectiveMerger
			.mergeLoose(List.of(ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(2L)).build(),
					ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(2L)).build()));
		assertEquals(DataScopeStorageType.DEPT, merged.getScopeType());
		assertEquals(List.of(2L), merged.getValues());
	}

	@Test
	@DisplayName("部门列合并忽略 POST 主体桶")
	void mergeLooseForDeptColumn_skipsPostBucket() {
		Map<String, ScopeGrant> map = Map.of("POST",
				ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT_AND_CHILD).values(List.of(99L)).build(),
				"DEPT", ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(1L)).build());
		ScopeGrant merged = ScopeGrantEffectiveMerger.mergeLooseForDeptColumn(map);
		assertNotNull(merged);
		assertEquals(DataScopeStorageType.DEPT, merged.getScopeType());
		assertEquals(List.of(1L), merged.getValues());
	}

	@Test
	@DisplayName("空集合返回 null")
	void mergeLoose_emptyReturnsNull() {
		assertNull(ScopeGrantEffectiveMerger.mergeLoose(List.of()));
	}

}
