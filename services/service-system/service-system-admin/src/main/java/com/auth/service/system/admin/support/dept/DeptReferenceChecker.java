package com.auth.service.system.admin.support.dept;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.service.system.admin.mapper.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * 部门写操作前的存在性与引用校验
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class DeptReferenceChecker {

	private final SysDeptMapper sysDeptMapper;

	/**
	 * 加载部门，不存在则抛业务异常（含禁用节点）
	 * @param deptId 部门 ID
	 * @return 部门实体
	 */
	public SysDeptEntity getExistingActive(Long deptId) {
		SysDeptEntity existing = sysDeptMapper.selectById(deptId);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, deptId);
		}
		return existing;
	}

	/**
	 * 规范化父部门 ID 并校验父部门存在（顶级跳过校验）
	 * @param rawParentId 原始父部门 ID
	 * @return 规范化后的父部门 ID
	 */
	public long normalizeAndRequireActiveParent(Long rawParentId) {
		long parentId = TreeParentIdUtil.normalize(rawParentId);
		if (parentId > TreeParentIdUtil.ROOT_PARENT_ID && sysDeptMapper.countActiveById(parentId) == 0) {
			throw new SystemBusinessException(SystemCommonResultCode.TREE_PARENT_UNAVAILABLE, parentId);
		}
		return parentId;
	}

	/**
	 * 批量写入前校验部门编码在请求内及库内均唯一
	 * @param deptCodes 待写入部门编码
	 */
	public void requireAbsentDeptCodes(List<String> deptCodes) {
		List<String> distinctCodes = BusinessKeyAssert.requireDistinct(deptCodes,
				code -> new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, code));
		if (CollUtil.isEmpty(distinctCodes)) {
			return;
		}

		List<SysDeptEntity> existing = sysDeptMapper.selectActiveByDeptCodes(distinctCodes);
		if (CollUtil.isNotEmpty(existing)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE,
					existing.get(0).getDeptCode());
		}
	}

}
