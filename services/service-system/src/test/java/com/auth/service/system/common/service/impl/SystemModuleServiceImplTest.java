package com.auth.service.system.common.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth.service.system.common.model.ModuleOption;

/**
 * {@link SystemModuleServiceImpl} 单元测试。
 * <p>
 * 该服务无外部依赖，直接基于枚举进行内存查询，因此不 mock 任何 Bean，仅验证过滤、排序与映射逻辑。
 * </p>
 *
 * @author Bunny
 */
@DisplayName("SystemModuleServiceImpl 系统模块")
@ExtendWith(MockitoExtension.class)
class SystemModuleServiceImplTest {

	private final SystemModuleServiceImpl service = new SystemModuleServiceImpl();

	@Test
	@DisplayName("无参查询返回全部 29 个模块，且每个字段都有值")
	void listReturnsAllModules() {
		List<ModuleOption> result;

		// ── 基础查询 ──────────────────────────────────────────────
		result = service.list(null, null, null);
		assertThat(result).hasSize(29);
		assertThat(result).allSatisfy(opt -> assertAll(() -> assertThat(opt.getName()).isNotBlank(),
				() -> assertThat(opt.getType()).isNotBlank(), () -> assertThat(opt.getLabel()).isNotBlank()));

		// limit=5 时只返回 5 条
		result = service.list(null, null, 5);
		assertThat(result).hasSize(5);

		// limit=0 时不限制条数，返回全部
		result = service.list(null, null, 0);
		assertThat(result).hasSize(29);

		// ── 按分类筛选 ────────────────────────────────────────────
		result = service.list(null, "SYS", null);
		assertThat(result).hasSize(15);
		assertThat(result).allMatch(opt -> "SYS".equals(opt.getType()));

		result = service.list(null, "USER", null);
		assertThat(result).hasSize(3);
		assertThat(result).allMatch(opt -> "USER".equals(opt.getType()));

		result = service.list(null, "LOG", null);
		assertThat(result).hasSize(3);
		assertThat(result).allMatch(opt -> "LOG".equals(opt.getType()));

		result = service.list(null, "GRANT", null);
		assertThat(result).hasSize(2);

		result = service.list(null, "POLICY", null);
		assertThat(result).hasSize(3);

		result = service.list(null, "AUDIT", null);
		assertThat(result).hasSize(3);

		// 按不存在的分类筛选，返回空列表
		result = service.list(null, "NOT_EXIST", null);
		assertThat(result).isEmpty();

		// ── 关键词模糊匹配 ────────────────────────────────────────
		// 关键词模糊匹配枚举 name，例如 'USER' 匹配出 USER 相关模块
		result = service.list("USER", null, null);
		assertThat(result).isNotEmpty();
		assertThat(result).allMatch(opt -> opt.getName().contains("USER"));

		// 关键词模糊匹配中文描述，例如 '日志' 匹配出所有日志模块
		result = service.list("日志", null, null);
		assertThat(result).isNotEmpty();
		assertThat(result).allMatch(opt -> opt.getLabel().contains("日志"));

		// 关键词忽略大小写匹配枚举 name，例如 'sys' 匹配 SYS 开头模块
		result = service.list("sys", null, null);
		assertThat(result).isNotEmpty();
		assertThat(result).allMatch(opt -> opt.getName().startsWith("SYS"));

		// 关键词无匹配时返回空列表
		result = service.list("__不存在的关键词__", null, null);
		assertThat(result).isEmpty();

		// ── 组合筛选 ──────────────────────────────────────────────
		// 关键词+分类组合：SYS 中搜索 '用户' 仅匹配 SYS_USER
		result = service.list("用户", "SYS", null);
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("SYS_USER");

		// 关键词+分类+limit 组合：AUDIT 中搜索 '日志' 限制 2 条
		result = service.list("日志", "AUDIT", 2);
		assertThat(result).hasSize(2);
		assertThat(result).allMatch(opt -> "AUDIT".equals(opt.getType()));
		assertThat(result).allMatch(opt -> opt.getLabel().contains("日志"));

		// ── 特殊处理 ──────────────────────────────────────────────
		// 关键词有前后空格时自动去除空白
		result = service.list("  角色  ", null, null);
		assertThat(result).isNotEmpty();
		assertThat(result).allMatch(opt -> opt.getName().contains("ROLE"));
	}

}
