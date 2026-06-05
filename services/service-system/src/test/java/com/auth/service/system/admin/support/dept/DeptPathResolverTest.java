package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.po.dept.SysDeptPathRowPO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link DeptPathResolver} 单元测试
 */
@DisplayName("DeptPathResolver 部门全路径解析")
@ExtendWith(MockitoExtension.class)
class DeptPathResolverTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	@InjectMocks
	private DeptPathResolver deptPathResolver;

	@Test
	@DisplayName("空 ID 列表返回空映射且不访问数据库")
	void resolve_whenEmptyIds_returnsEmptyMap() {
		Map<Long, String> result = deptPathResolver.resolve(Collections.emptyList());

		assertThat(result).isEmpty();
		verifyNoInteractions(sysDeptMapper);
	}

	@Test
	@DisplayName("按 descendant_id 组装根到叶的全路径映射")
	void resolve_whenIdsGiven_returnsPathById() {
		SysDeptPathRowPO root = new SysDeptPathRowPO();
		root.setId(1L);
		root.setDeptPath("总部");

		SysDeptPathRowPO child = new SysDeptPathRowPO();
		child.setId(3L);
		child.setDeptPath("总部/研发/后端");

		when(sysDeptMapper.selectDeptPathByIds(List.of(1L, 3L))).thenReturn(List.of(root, child));

		Map<Long, String> result = deptPathResolver.resolve(List.of(1L, 3L));

		assertThat(result).containsEntry(1L, "总部").containsEntry(3L, "总部/研发/后端");
	}

}
