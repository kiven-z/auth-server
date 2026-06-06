package com.auth.service.system.authorization.dispatch.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.dto.invalidation.GrantSubjectKey;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 授权失效格式 {operation}:{bizKey}
 *
 * @author Bunny
 */
@UtilityClass
public final class InvalidationSourceBizIds {

    /**
     * 组装 sourceBizId
     *
     * @param operation 操作动词，如 move、update
     * @param bizKey    业务主键片段
     * @return sourceBizId
     */
    public static String of(String operation, String bizKey) {
        return CharSequenceUtil.blankToDefault(operation, "invalidate") + ":" + bizKey;
    }

    /**
     * 将 Long 型主键列表拼接为逗号分隔串
     *
     * @param ids 主键列表
     * @return 逗号分隔串
     */
    public static String joinLongIds(Collection<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * 将字符串码列表拼接为逗号分隔串
     *
     * @param codes 码列表
     * @return 逗号分隔串
     */
    public static String joinCodes(Collection<String> codes) {
        return String.join(",", codes);
    }

    /**
     * 将 grant 主体键格式化为 type:id 列表
     *
     * @param subjects 授权主体键
     * @return 逗号分隔的主体描述
     */
    public static String formatGrantSubjects(Collection<GrantSubjectKey> subjects) {
        return subjects.stream()
                .map(subject -> subject.subjectType().name().toLowerCase() + ":" + subject.subjectId())
                .collect(Collectors.joining(","));
    }

}
