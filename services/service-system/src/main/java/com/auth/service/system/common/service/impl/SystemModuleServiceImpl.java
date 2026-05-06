package com.auth.service.system.common.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.common.model.ModuleOption;
import com.auth.service.system.common.model.SystemModule;
import com.auth.service.system.common.service.SystemModuleService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * 系统模块服务实现。
 * <p>
 * 基于 {@link SystemModule} 枚举提供静态模块列表，支持关键词模糊搜索与分类筛选。
 * 由于模块数据由枚举定义、不会频繁变更，此处为本地纯内存查询，无需引入缓存层。
 * </p>
 *
 * @author Bunny
 */
@Service
public class SystemModuleServiceImpl implements SystemModuleService {

    /**
     * 获取模块选项列表。
     *
     * @param keyword 搜索关键词，按表名、描述、分类模糊匹配；为空时不作为过滤条件
     * @param type    模块分类筛选；为空时不作为过滤条件
     * @param limit   返回条数上限
     * @return 匹配的模块选项列表
     */
    @Override
    public List<ModuleOption> list(String keyword, Integer limit) {
        Stream<SystemModule> stream = Arrays.stream(SystemModule.values());

        // 按分类筛选
        if (CharSequenceUtil.isNotBlank(type)) {
            stream = stream.filter(m -> type.equals(m.getType()));
        }

        // 按关键词模糊匹配（表名、描述、分类）
        if (CharSequenceUtil.isNotBlank(keyword)) {
            String kw = keyword.trim().toLowerCase();
            stream = stream.filter(m ->
                    m.getTable().toLowerCase().contains(kw)
                            || m.getDescription().contains(kw)
                            || m.getType().toLowerCase().contains(kw)
            );
        }

        // 应用条数上限（如果 limit 为 null 或小于等于 0，不限制）
        if (limit != null && limit > 0) {
            stream = stream.limit(limit);
        }

        return stream.map(m -> ModuleOption.builder()
                        .label(m.getDescription())
                        .type(m.getType())
                        .description(m.getDescription())
                        .build())
                .toList();
    }

}
