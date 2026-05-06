package com.auth.module.security.contract.api.authorization;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.auth.module.security.contract.api.granttable.ScopeGrantEffectiveMerger;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;

class ScopeGrantEffectiveMergerTest {

    @Test
    @DisplayName("ALL 覆盖其它类型")
    void mergeLoose_allWins() {
        ScopeGrant merged = ScopeGrantEffectiveMerger.mergeLoose(List.of(
                ScopeGrant.builder().scopeType(ScopeGrantType.SELF).build(),
                ScopeGrant.builder().scopeType(ScopeGrantType.ALL).build()));
        assertEquals(ScopeGrantType.ALL, merged.getScopeType());
    }

    @Test
    @DisplayName("合并 CUSTOM/NODE 取值并降为 CUSTOM IN")
    void mergeLoose_unionsIds() {
        ScopeGrant merged = ScopeGrantEffectiveMerger.mergeLoose(List.of(
                ScopeGrant.builder().scopeType(ScopeGrantType.NODE_ONLY).values(List.of("1", "2")).build(),
                ScopeGrant.builder().scopeType(ScopeGrantType.CUSTOM).values(List.of("2", "3")).build()));
        assertEquals(ScopeGrantType.CUSTOM, merged.getScopeType());
        assertEquals(List.of("1", "2", "3"), merged.getValues());
    }

    @Test
    @DisplayName("部门列合并忽略 POST 主体桶")
    void mergeLooseForDeptColumn_skipsPostBucket() {
        Map<String, ScopeGrant> map = Map.of(
                "POST",
                ScopeGrant.builder().scopeType(ScopeGrantType.CUSTOM).values(List.of("99")).build(),
                "DEPT",
                ScopeGrant.builder().scopeType(ScopeGrantType.NODE_ONLY).values(List.of("1")).build());
        ScopeGrant merged = ScopeGrantEffectiveMerger.mergeLooseForDeptColumn(map);
        assertEquals(ScopeGrantType.CUSTOM, merged.getScopeType());
        assertEquals(List.of("1"), merged.getValues());
    }

    @Test
    @DisplayName("空集合返回 null")
    void mergeLoose_emptyReturnsNull() {
        assertNull(ScopeGrantEffectiveMerger.mergeLoose(List.of()));
    }
}
