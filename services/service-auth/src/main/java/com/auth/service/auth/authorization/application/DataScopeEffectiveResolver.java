package com.auth.service.auth.authorization.application;

import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeDimension;
import com.auth.service.auth.authorization.application.datascope.DeptDataScopeResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户数据权限生效范围解析（部门维）
 *
 * @author Bunny
 */
@Slf4j
@Service
public class DataScopeEffectiveResolver {

    private final DeptDataScopeResolver deptDataScopeResolver;

    public DataScopeEffectiveResolver(DeptDataScopeResolver deptDataScopeResolver) {
        this.deptDataScopeResolver = deptDataScopeResolver;
    }

    /**
     * 按用户与角色码解析生效数据权限（单键 DEPT::default）
     * TODO 考虑放在 datascope 目录下并删除此类
     *
     * @param userId    用户 ID
     * @param roleCodes 角色码列表
     * @return 生效数据权限 Map
     */
    public Map<String, ScopeGrant> resolveByUserIdAndRoles(Long userId, List<String> roleCodes) {
        if (userId == null) {
            return Map.of();
        }
        ScopeGrant grant = deptDataScopeResolver.resolveEffectiveGrant(userId, roleCodes);
        return Map.of(DataScopeDimension.DEPT.defaultProfileKey(), grant);
    }

}
