package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.DeptReferenceMapper;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.support.tree.TreeMoveTargetValidator;
import com.auth.service.system.admin.support.tree.TreeParentIdNormalizer;
import com.auth.service.system.admin.support.tree.TreeParentIds;
import com.auth.service.system.common.exception.SystemBusinessException;
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

	private final DeptReferenceMapper deptReferenceMapper;

	/**
	 * 将 parentId 规范为可比较的父部门 ID（null 或负数视为顶级）
	 * @param parentId 原始父部门 ID
	 * @return 规范化后的父部门 ID
	 */
	public static long normalizeParentId(Long parentId) {
		// FIXME 精简,直接调用 TreeParentIdNormalizer.normalize
		return TreeParentIdNormalizer.normalize(parentId);
	}

	/**
	 * 从已落库实体解析可比较的父部门 ID
	 * @param entity 部门实体
	 * @return 规范化后的父部门 ID
	 */
	public static long resolveParentId(SysDeptEntity entity) {
		// FIXME 精简,直接调用 normalizeParentId
		return normalizeParentId(entity.getParentId());
	}

	/**
	 * 加载未删除部门，不存在则抛业务异常
	 * @param deptId 部门 ID
	 * @return 部门实体
	 */
	public SysDeptEntity getExistingActive(Long deptId) {
		SysDeptEntity existing = sysDeptMapper.selectById(deptId);
		if (existing == null) {
			throw new SystemBusinessException(SystemAdminResultCode.DEPT_NOT_FOUND, deptId);
		}
		return existing;
	}

	/**
	 * 规范化父部门 ID 并校验父部门存在（顶级跳过校验）
	 * @param rawParentId 原始父部门 ID
	 * @return 规范化后的父部门 ID
	 */
	public long normalizeAndRequireActiveParent(Long rawParentId) {
		long parentId = normalizeParentId(rawParentId);
		if (parentId > TreeParentIds.ROOT_PARENT_ID && sysDeptMapper.countActiveById(parentId) == 0) {
			throw new SystemBusinessException(SystemAdminResultCode.DEPT_PARENT_NOT_FOUND, parentId);
		}
		return parentId;
	}

	/**
	 * 移动目标不能是自身或自身后代
	 * @param deptId 被移动部门
	 * @param newParentId 新父部门
	 */
	public void requireMoveTargetValid(Long deptId, Long newParentId) {
		TreeMoveTargetValidator.requireValid(deptId, newParentId,
				(ancestorId, nodeId) -> deptReferenceMapper.countDescendantRelation(ancestorId, nodeId) > 0,
				new SystemBusinessException(SystemAdminResultCode.DEPT_MOVE_TO_SELF, deptId),
				new SystemBusinessException(SystemAdminResultCode.DEPT_MOVE_TO_DESCENDANT, deptId, newParentId));
	}

	/**
	 * 逻辑删除前：无未删除的直接子部门
	 * @param deptId 部门 ID
	 */
	public void requireNoActiveChildren(Long deptId) {
		if (sysDeptMapper.countActiveDirectChildren(deptId) > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.DEPT_HAS_CHILDREN, deptId);
		}
	}

	/**
	 * 逻辑删除前：子树内无用户、授权、岗位引用
	 * @param deptId 部门 ID
	 */
	public void requireSubtreeUnused(Long deptId) {
		if (deptReferenceMapper.countUserDeptInSubtree(deptId) > 0
				|| deptReferenceMapper.countGrantDeptSubjectInSubtree(deptId) > 0
				|| deptReferenceMapper.countPostInSubtree(deptId) > 0) {
			throw new SystemBusinessException(SystemAdminResultCode.DEPT_IN_USE, deptId);
		}
	}

}
