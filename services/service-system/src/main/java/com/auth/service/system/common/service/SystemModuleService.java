package com.auth.service.system.common.service;

import java.util.List;

import com.auth.service.system.common.model.ModuleOption;

/**
 * 系统模块服务。
 * <p>
 * 系统模块基于枚举定义，支持前端远程搜索下拉框按关键词模糊匹配枚举 name 或中文描述。
 * </p>
 *
 * @author Bunny
 */
public interface SystemModuleService {

	/**
	 * 获取模块选项列表（支持远程搜索）。
	 * @param keyword 搜索关键词，模糊匹配枚举 name 或中文描述（为空时返回全部）
	 * @param type 模块分类筛选（SYS / USER / GRANT / POLICY / AUDIT / LOG）
	 * @param limit 返回条数上限
	 * @return 模块选项列表
	 */
	List<ModuleOption> list(String keyword, String type, Integer limit);

}
