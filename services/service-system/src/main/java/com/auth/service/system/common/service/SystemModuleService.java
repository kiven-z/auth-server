package com.auth.service.system.common.service;

import com.auth.service.system.common.model.ModuleOption;

import java.util.List;

/**
 * 系统模块服务。
 * <p>
 * 系统模块基于数据库业务表进行枚举定义，支持前端远程搜索下拉框按关键词模糊匹配。
 * </p>
 *
 * @author Bunny
 */
public interface SystemModuleService {

    /**
     * 获取模块选项列表（支持远程搜索）。
     *
     * @param keyword 搜索关键词，模糊匹配表名、描述或分类（为空时返回全部）
     * @param limit   返回条数上限
     * @return 模块选项列表
     */
    List<ModuleOption> list(String keyword, Integer limit);

}
