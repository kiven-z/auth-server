package com.auth.service.auth.authorization.domain.datascope;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.DataScopeKeyUtil;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.DataScopeProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataScopeBuilderTest {

    @Test
    @DisplayName("用户授权优先于角色授权")
    void userScope_shouldOverrideRoleScope() {
        // 同一资源键下存在 USER 和 ROLE 时，优先使用 USER
        List<DataScopeProjection> projections = List.of(projection("USER", "default", "SELF", "[]"),
                projection("ROLE", "default", "ALL", "[]"));

        Map<String, ScopeGrant> scopes = DataScopeBuilder.buildDataScopes(projections);
        ScopeGrant grant = scopes.get(DataScopeKeyUtil.compose("DEPT", "default"));

        assertEquals(ScopeGrantType.SELF, grant.getScopeType());
    }

    @Test
    @DisplayName("角色侧缺少用户直连时执行宽松合并")
    void roleScopes_shouldMergeLoosely() {
        // 无 USER 直连时，ROLE/DEPT 片段应按宽松规则合并
        List<DataScopeProjection> projections = List.of(projection("ROLE", "order", "NODE_ONLY", "[\"1\",\"2\"]"),
                projection("DEPT", "order", "CUSTOM", "[\"2\",\"3\"]"));

        Map<String, ScopeGrant> scopes = DataScopeBuilder.buildDataScopes(projections);
        ScopeGrant grant = scopes.get(DataScopeKeyUtil.compose("DEPT", "order"));

        assertEquals(ScopeGrantType.CUSTOM, grant.getScopeType());
        assertEquals(List.of("1", "2", "3"), grant.getValues());
    }

    @Test
    @DisplayName("空输入返回空范围")
    void emptyProjection_shouldReturnEmptyMap() {
        // 无授权片段时构建器应返回空 map
        Map<String, ScopeGrant> scopes = DataScopeBuilder.buildDataScopes(List.of());
        assertTrue(scopes.isEmpty());
    }

    @Test
    @DisplayName("构建轨迹包含命中来源与生效类型")
    void buildWithTrace_shouldExposeResolutionTrace() {
        // 构建轨迹应包含来源与最终类型，便于运维排查
        List<DataScopeProjection> projections = List.of(projection("ROLE", "default", "NODE_ONLY", "[\"10\"]"));

        DataScopeBuildResult result = DataScopeBuilder.buildDataScopesWithTrace(projections);

        assertEquals(1, result.getDataScopes().size());
        assertEquals(1, result.getTraces().size());
        assertEquals("ROLE_MERGE", result.getTraces().get(0).getSource());
        assertEquals(ScopeGrantType.CUSTOM.name(), result.getTraces().get(0).getEffectiveScopeType());
    }

    private DataScopeProjection projection(String subjectType, String resourceCode, String scopeType, String values) {
        DataScopeProjection projection = new DataScopeProjection();
        projection.setSubjectType(subjectType);
        projection.setScopeResourceCode(resourceCode);
        projection.setDimensionType("DEPT");
        projection.setScopeType(scopeType);
        projection.setScopeSubjectIds(values);
        return projection;
    }

}
