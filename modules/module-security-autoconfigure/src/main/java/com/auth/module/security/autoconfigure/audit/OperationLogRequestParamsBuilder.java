package com.auth.module.security.autoconfigure.audit;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * 操作日志请求参数摘要构建（长度与表字段对齐）。
 *
 * @author Bunny
 */
@UtilityClass
public class OperationLogRequestParamsBuilder {

    /**
     * 请求文本最大长度（与表字段设计对齐，约 4KB）。
     */
    public static final int MAX_REQUEST_TEXT = 4096;

    /**
     * 组装请求参数摘要：GET 使用 query string；其它方法序列化 parameterMap。
     *
     * @param request      HTTP 请求
     * @param record       是否记录
     * @param objectMapper JSON 序列化
     * @return 摘要或 {@code null}
     */
    public static String build(HttpServletRequest request, boolean record, ObjectMapper objectMapper) {
        if (!record) {
            return null;
        }
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String toEmpty = CharSequenceUtil.nullToEmpty(request.getQueryString());
            return CharSequenceUtil.subPre(toEmpty, MAX_REQUEST_TEXT);
        }

        Map<String, String[]> map = request.getParameterMap();
        if (MapUtil.isEmpty(map)) {
            return null;
        }
        try {
            return CharSequenceUtil.subPre(objectMapper.writeValueAsString(map), MAX_REQUEST_TEXT);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

}
