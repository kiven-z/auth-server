package com.auth.service.system.admin.support.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.admin.mapper.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按部门编码批量解析部门主键，供导入阶段复用。
 *
 * @author bunny
 */
@RequiredArgsConstructor
@Component
public class DeptCodeIdResolver {

	private final SysDeptMapper sysDeptMapper;

	/**
	 * 从行集合中提取部门编码并批量解析为主键映射。
	 * @param rows 行数据集合
	 * @param deptCodeGetter 部门编码提取函数
	 * @param <R> 行数据类型
	 * @return 部门编码 → 部门主键
	 */
	public <R> Map<String, Long> resolveIdsByCodes(List<R> rows, Function<R, String> deptCodeGetter) {
		if (CollUtil.isEmpty(rows)) {
			return Map.of();
		}

		List<String> deptCodes = rows.stream()
			.map(deptCodeGetter)
			.filter(CharSequenceUtil::isNotBlank)
			.distinct()
			.toList();
		if (CollUtil.isEmpty(deptCodes)) {
			return Map.of();
		}

		return sysDeptMapper.selectActiveByDeptCodes(deptCodes)
			.stream()
			.collect(Collectors.toMap(SysDeptEntity::getDeptCode, SysDeptEntity::getId, (left, right) -> left,
					HashMap::new));
	}

}
