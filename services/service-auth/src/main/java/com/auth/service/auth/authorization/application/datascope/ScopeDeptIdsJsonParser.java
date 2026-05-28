package com.auth.service.auth.authorization.application.datascope;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 解析 role_scope / user_scope 中 scope_dept_ids JSON 为部门 ID 字符串列表
 * FIXME 改为工具进行解析
 *
 * @author Bunny
 */
@UtilityClass
@Slf4j
public class ScopeDeptIdsJsonParser {

    /**
     * 解析 scope_dept_ids 列
     *
     * @param rawJson JSON 数组字符串或逗号分隔
     * @return 非空部门 ID 字符串列表
     */
    public static List<String> parse(String rawJson) {
        if (CharSequenceUtil.isBlank(rawJson)) {
            return List.of();
        }
        String trimmed = CharSequenceUtil.trim(rawJson);
        if (trimmed.startsWith("[")) {
            try {
                return JSONUtil.parseArray(trimmed)
                        .stream()
                        .map(String::valueOf)
                        .map(CharSequenceUtil::trim)
                        .filter(CharSequenceUtil::isNotBlank)
                        .filter(ScopeDeptIdsJsonParser::isNumericId)
                        .distinct()
                        .toList();
            } catch (Exception exception) {
                log.warn("Failed to parse scope_dept_ids JSON: {}", exception.getMessage());
            }
        }
        return Arrays.stream(trimmed.split(","))
                .map(CharSequenceUtil::trim)
                .filter(CharSequenceUtil::isNotBlank)
                .filter(ScopeDeptIdsJsonParser::isNumericId)
                .distinct()
                .toList();
    }

    /**
     * Long 部门 ID 转字符串列表
     *
     * @param deptIds 部门 ID
     * @return 字符串列表
     */
    public static List<String> fromLongIds(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return List.of();
        }
        return deptIds.stream().filter(Objects::nonNull).map(String::valueOf).distinct().toList();
    }

    private static boolean isNumericId(String value) {
        return value.matches("\\d+");
    }

}
