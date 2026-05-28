package com.auth.module.security.contract.api.datascope;

import lombok.experimental.UtilityClass;

import java.util.Locale;

/**
 * 数据权限键编解码（仅格式，不含产品默认维度/资源）
 *
 * <p>
 * 编码格式：{@code DIMENSION::RESOURCE_CODE}，如 {@code DEPT::default}。
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class DataScopeKeyUtil {

    private static final String SEPARATOR = "::";

    /**
     * 组合数据权限键
     *
     * @param dimension    维度编码
     * @param resourceCode 资源键
     * @return 标准化后的组合键
     */
    public static String compose(DataScopeDimension dimension, String resourceCode) {
        if (dimension == null) {
            throw new IllegalArgumentException("dimension must not be null");
        }
        return compose(dimension.code(), resourceCode);
    }

    /**
     * 组合数据权限键
     *
     * @param dimension    维度编码
     * @param resourceCode 资源键
     * @return 标准化后的组合键
     */
    public static String compose(String dimension, String resourceCode) {
        // TODO 进行简化，没必要去空字符串
        return normalizeDimension(dimension) + SEPARATOR + normalizeResourceCode(resourceCode);
    }

    /**
     * 标准化维度编码
     *
     * @param dimension 输入维度
     * @return 大写维度编码
     */
    public static String normalizeDimension(String dimension) {
        // TODO 进行简化，没必要去空字符串
        if (dimension == null || dimension.isBlank()) {
            throw new IllegalArgumentException("dimension must not be blank");
        }
        return dimension.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 标准化资源键
     *
     * @param resourceCode 输入资源键
     * @return trim 后的资源键
     */
    public static String normalizeResourceCode(String resourceCode) {
        // TODO 进行简化，没必要去空字符串
        if (resourceCode == null || resourceCode.isBlank()) {
            throw new IllegalArgumentException("resourceCode must not be blank");
        }
        return resourceCode.trim();
    }

}
