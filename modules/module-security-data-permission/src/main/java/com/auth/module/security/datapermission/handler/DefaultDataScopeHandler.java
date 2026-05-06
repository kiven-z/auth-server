package com.auth.module.security.datapermission.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.granttable.ScopeGrantEffectiveMerger;
import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.auth.module.security.datapermission.annotation.DataScope;

import java.util.List;
import java.util.regex.Pattern;

import static com.auth.module.security.contract.constants.PermissionConstant.*;

/**
 * 默认处理器用于部门/自身过滤
 *
 * @author Bunny
 */
public class DefaultDataScopeHandler implements DataScopeHandler {

    private static final Pattern SAFE_SQL_IDENT = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private static boolean isAdminRoleOrWildcard(List<String> roles, List<String> permissions) {
        if (isAdminRole(roles)) {
            return true;
        }

        return isAdminPermission(permissions);
    }

    private static String safeAliasPrefix(String alias) {
        if (CharSequenceUtil.isBlank(alias)) {
            return "";
        }
        String trimmed = CharSequenceUtil.trim(alias);
        requireSafeIdent(trimmed);
        return trimmed + ".";
    }

    private static String requireSafeIdent(String ident) {
        String trimmed = CharSequenceUtil.trimToEmpty(ident);
        if (!SAFE_SQL_IDENT.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Unsafe SQL identifier: " + ident);
        }
        return trimmed;
    }

    /**
     * 构建当前用户的 SQL 条件（仅 USER/ROLE/DEPT 三类主体的范围合并后作用于组织列）
     */
    @Override
    public String buildCondition(AuthProfile profile, DataScope dataScope) {
        if (profile == null || dataScope == null) {
            return null;
        }

        Long userId = profile.getUserId();
        if (isSuperAdmin(userId)) {
            return null;
        }

        if (isAdminRoleOrWildcard(profile.getRoles(), profile.getPermissions())) {
            return null;
        }

        ScopeGrant effective = ScopeGrantEffectiveMerger.mergeLooseForDeptColumn(profile.getDataScopes());
        if (effective == null || effective.getScopeType() == null || effective.getScopeType() == ScopeGrantType.ALL) {
            return null;
        }

        String aliasPrefix = safeAliasPrefix(dataScope.alias());
        String userColumn = requireSafeIdent(dataScope.userColumn());
        String deptColumn = requireSafeIdent(dataScope.deptColumn());

        if (effective.getScopeType() == ScopeGrantType.SELF) {
            return aliasPrefix + userColumn + " = " + userId;
        }

        if (effective.getScopeType() == ScopeGrantType.NODE_ONLY || effective.getScopeType() == ScopeGrantType.NODE_AND_DESC
                || effective.getScopeType() == ScopeGrantType.CUSTOM) {
            List<String> deptIds = effective.getValues();
            if (deptIds == null || deptIds.isEmpty()) {
                return "1 = 0";
            }
            String in = deptIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
            return aliasPrefix + deptColumn + " IN (" + in + ")";
        }

        return null;
    }

}
