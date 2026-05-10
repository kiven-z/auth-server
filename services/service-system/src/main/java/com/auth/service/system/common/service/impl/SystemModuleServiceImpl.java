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
	 * {@inheritDoc}
	 */
	@Override
	public List<ModuleOption> list(String keyword, String type, Integer limit) {
		Stream<SystemModule> stream = Arrays.stream(SystemModule.values());

		// 按分类筛选
		if (CharSequenceUtil.isNotBlank(type)) {
			stream = stream.filter(m -> type.equals(m.getType()));
		}

		// 按关键词模糊匹配（枚举 name 或中文描述）
		if (CharSequenceUtil.isNotBlank(keyword)) {
			String kw = keyword.trim().toLowerCase();
			stream = stream.filter(m -> m.name().toLowerCase().contains(kw) || m.getDescription().contains(kw));
		}

		// 应用条数上限（如果 limit 为 null 或小于等于 0，不限制）
		if (limit != null && limit > 0) {
			stream = stream.limit(limit);
		}

		return stream
			.map(m -> ModuleOption.builder().name(m.name()).type(m.getType()).label(m.getDescription()).build())
			.toList();
	}

}
