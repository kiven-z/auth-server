package com.auth.service.system.admin.support.dept;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.mapper.dept.SysDeptMapper;
import com.auth.service.system.admin.model.po.dept.SysDeptPathRowPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于闭包表解析部门从根到叶的名称路径。
 *
 * @author bunny
 */
@RequiredArgsConstructor
@Component
public class DeptPathResolver {

	private final SysDeptMapper sysDeptMapper;

	/**
	 * 批量解析部门全路径。
	 * @param ids 部门主键列表
	 * @return 部门 ID → 全路径（如 {@code 总部/研发/后端}）
	 */
	public Map<Long, String> resolve(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return Collections.emptyMap();
		}

		List<SysDeptPathRowPO> rows = sysDeptMapper.selectDeptPathByIds(ids);
		return rows.stream()
			.collect(Collectors.toMap(SysDeptPathRowPO::getId, SysDeptPathRowPO::getDeptPath, (left, right) -> left));
	}

}
