package com.auth.service.system.admin.support.dept;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.service.system.admin.mapper.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门写操作前的存在性与引用校验
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
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
			log.warn("department not found: id={}", deptId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}
		return existing;
	}

	/**
	 * 批量校验部门 ID 均存在（含停用节点）；空列表直接返回
	 * <p>
	 * 调用方需保证 ID 已去重
	 * </p>
	 * @param deptIds 部门 ID 列表（已去重）
	 */
	public void requireExistingDeptIds(List<Long> deptIds) {
		if (CollUtil.isEmpty(deptIds)) {
			return;
		}

		List<SysDeptEntity> found = sysDeptMapper.selectByIds(deptIds);
		Set<Long> foundIds = found.stream().map(SysDeptEntity::getId).collect(Collectors.toSet());
		if (foundIds.size() != deptIds.size()) {
			Long invalidId = deptIds.stream()
				.filter(id -> !foundIds.contains(id))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("dept id mismatch without invalid id"));
			log.warn("Scope dept reference invalid: id={}", invalidId);
			throw new SystemBusinessException(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		}
	}

	/**
	 * 规范化父部门 ID 并校验父部门存在（顶级跳过校验）
	 * @param rawParentId 原始父部门 ID
	 * @return 规范化后的父部门 ID
	 */
	public long normalizeAndRequireActiveParent(Long rawParentId) {
		long parentId = TreeParentIdUtil.normalize(rawParentId);
		if (parentId > TreeParentIdUtil.ROOT_PARENT_ID && sysDeptMapper.countActiveById(parentId) == 0) {
			log.warn("Tree parent unavailable: parentId={}", parentId);
			throw new SystemBusinessException(SystemCommonResultCode.TREE_PARENT_UNAVAILABLE);
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
