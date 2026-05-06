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
		List<ModuleOption> result = service.list(null, null, null);

		assertThat(result).hasSize(29);
		assertThat(result).allSatisfy(opt -> assertAll(() -> assertThat(opt.getName()).isNotBlank(),
				() -> assertThat(opt.getType()).isNotBlank(), () -> assertThat(opt.getLabel()).isNotBlank()));
	}

	@Test
	@DisplayName("limit=5 时只返回 5 条")
	void listRespectsLimit() {
		List<ModuleOption> result = service.list(null, null, 5);

		assertThat(result).hasSize(5);
	}

	@Test
	@DisplayName("limit=0 时不限制条数，返回全部")
	void listWithZeroLimitReturnsAll() {
		List<ModuleOption> result = service.list(null, null, 0);

		assertThat(result).hasSize(29);
	}

	@Test
	@DisplayName("按分类 SYS 筛选，返回 15 个系统业务模块")
	void listFiltersByTypeSys() {
		List<ModuleOption> result = service.list(null, "SYS", null);

		assertThat(result).hasSize(15);
		assertThat(result).allMatch(opt -> "SYS".equals(opt.getType()));
	}

	@Test
	@DisplayName("按分类 USER 筛选，返回 3 个用户相关模块")
	void listFiltersByTypeUser() {
		List<ModuleOption> result = service.list(null, "USER", null);

		assertThat(result).hasSize(3);
		assertThat(result).allMatch(opt -> "USER".equals(opt.getType()));
	}

	@Test
	@DisplayName("按分类 LOG 筛选，返回 3 个操作日志模块")
	void listFiltersByTypeLog() {
		List<ModuleOption> result = service.list(null, "LOG", null);

		assertThat(result).hasSize(3);
		assertThat(result).allMatch(opt -> "LOG".equals(opt.getType()));
	}

	@Test
	@DisplayName("按分类 GRANT 筛选，返回 2 个授权模块")
	void listFiltersByTypeGrant() {
		List<ModuleOption> result = service.list(null, "GRANT", null);

		assertThat(result).hasSize(2);
	}

	@Test
	@DisplayName("按分类 POLICY 筛选，返回 3 个策略模块")
	void listFiltersByTypePolicy() {
		List<ModuleOption> result = service.list(null, "POLICY", null);

		assertThat(result).hasSize(3);
	}

	@Test
	@DisplayName("按分类 AUDIT 筛选，返回 3 个审计日志模块")
	void listFiltersByTypeAudit() {
		List<ModuleOption> result = service.list(null, "AUDIT", null);

		assertThat(result).hasSize(3);
	}

	@Test
	@DisplayName("按不存在的分类筛选，返回空列表")
	void listWithNonExistentTypeReturnsEmpty() {
		List<ModuleOption> result = service.list(null, "NOT_EXIST", null);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("关键词模糊匹配枚举 name，例如 'USER' 匹配出 USER 相关模块")
	void listKeywordMatchesEnumName() {
		List<ModuleOption> result = service.list("USER", null, null);

		assertThat(result).isNotEmpty();
		assertThat(result).allMatch(opt -> opt.getName().contains("USER"));
	}

	@Test
	@DisplayName("关键词模糊匹配中文描述，例如 '日志' 匹配出所有日志模块")
	void listKeywordMatchesDescription() {
		List<ModuleOption> result = service.list("日志", null, null);

		assertThat(result).isNotEmpty();
		assertThat(result).allMatch(opt -> opt.getLabel().contains("日志"));
	}

	@Test
	@DisplayName("关键词忽略大小写匹配枚举 name，例如 'sys' 匹配 SYS 开头模块")
	void listKeywordIsCaseInsensitive() {
		List<ModuleOption> result = service.list("sys", null, null);

		assertThat(result).isNotEmpty();
		assertThat(result).allMatch(opt -> opt.getName().startsWith("SYS"));
	}

	@Test
	@DisplayName("关键词无匹配时返回空列表")
	void listWithNoMatchReturnsEmpty() {
		List<ModuleOption> result = service.list("__不存在的关键词__", null, null);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("关键词+分类组合筛选：SYS 中搜索 '用户' 仅匹配 SYS_USER")
	void listCombinesKeywordAndType() {
		List<ModuleOption> result = service.list("用户", "SYS", null);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("SYS_USER");
	}

	@Test
	@DisplayName("关键词+分类+limit 组合：AUDIT 中搜索 '日志' 限制 2 条")
	void listCombinesAllParams() {
		List<ModuleOption> result = service.list("日志", "AUDIT", 2);

		assertThat(result).hasSize(2);
		assertThat(result).allMatch(opt -> "AUDIT".equals(opt.getType()));
		assertThat(result).allMatch(opt -> opt.getLabel().contains("日志"));
	}

	@Test
	@DisplayName("关键词有前后空格时自动去除空白")
	void listTrimsKeyword() {
		List<ModuleOption> result = service.list("  角色  ", null, null);

		assertThat(result).isNotEmpty();
		assertThat(result).allMatch(opt -> opt.getName().contains("ROLE"));
	}

}
