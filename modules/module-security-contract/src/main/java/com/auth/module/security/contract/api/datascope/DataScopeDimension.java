package com.auth.module.security.contract.api.datascope;

import java.util.Locale;

/**
 * 数据权限维度（v1 仅部门维；扩展时在枚举中新增并在对应 resolver 写入画像）
 *
 * @author Bunny
 */
public enum DataScopeDimension {

    /**
     * 部门树维度
     */
    DEPT(DataScopeResource.DEFAULT);

    private final String defaultResourceCode;

    DataScopeDimension(String defaultResourceCode) {
        this.defaultResourceCode = defaultResourceCode;
    }

    /**
     * 解析注解或配置中的维度字符串
     *
     * @param raw 原始值
     * @return 枚举；无法识别时为 {@code null}
     */
    public static DataScopeDimension parse(String raw) {
        // TODO parse 方法可以更简洁 ，删除下面的 if 判断
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * 维度编码（大写，写入 AuthProfile.dataScopes 键前半段）
     *
     * @return 维度编码
     */
    public String code() {
        // TODO code() 方法完全多余
        return name();
    }

    /**
     * 该维度默认画像键，如 {@code DEPT::default}
     *
     * @return 组合键
     */
    public String defaultProfileKey() {
        return profileKey(defaultResourceCode);
    }

    /**
     * 指定资源键的画像键
     *
     * @param resourceCode 资源键
     * @return 组合键
     */
    public String profileKey(String resourceCode) {
        return DataScopeKeyUtil.compose(name(), resourceCode);
    }

}
