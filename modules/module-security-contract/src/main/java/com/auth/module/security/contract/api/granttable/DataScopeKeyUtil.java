package com.auth.module.security.contract.api.granttable;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.util.Locale;

/**
 * 数据权限键工具
 *
 * <p>
 * 编码格式：{@code DIMENSION::RESOURCE_CODE}，如 {@code DEPT::default}。
 * </p>
 *
 * @author Bunny
 */
@UtilityClass
public class DataScopeKeyUtil {

    /**
     * 默认资源键
     * FIXME 应修复不放在这
     */
    public static final String DEFAULT_RESOURCE_CODE = "default";

    /**
     * 默认维度
     * FIXME 应修复不放在这
     */
    public static final String DEFAULT_DIMENSION = "DEPT";

    /**
     * 组合数据权限键
     *
     * @param dimension    维度（如 DEPT/POST）
     * @param resourceCode 资源键
     * @return 标准化后的组合键
     */
    public static String compose(String dimension, String resourceCode) {
        String normalizedDimension = normalizeDimension(dimension);
        String normalizedResourceCode = normalizeResourceCode(resourceCode);
        return normalizedDimension + "::" + normalizedResourceCode;
    }

    /**
     * 标准化维度
     *
     * @param dimension 输入维度
     * @return 标准化维度
     */
    public static String normalizeDimension(String dimension) {
        if (CharSequenceUtil.isBlank(dimension)) {
            return DEFAULT_DIMENSION;
        }
        return CharSequenceUtil.trim(dimension).toUpperCase(Locale.ROOT);
    }

    /**
     * 标准化资源键
     *
     * @param resourceCode 输入资源键
     * @return 标准化资源键
     */
    public static String normalizeResourceCode(String resourceCode) {
        if (CharSequenceUtil.isBlank(resourceCode)) {
            return DEFAULT_RESOURCE_CODE;
        }
        return CharSequenceUtil.trim(resourceCode);
    }

}
