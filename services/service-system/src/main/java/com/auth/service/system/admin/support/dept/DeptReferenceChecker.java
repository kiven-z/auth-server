package com.auth.service.system.admin.support.dept;

import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.common.core.utils.TreeRelationUtil;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.DeptReferenceMapper;
import com.auth.service.system.admin.mapper.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
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
		long parentId = TreeParentIdUtil.normalize(rawParentId);
		if (parentId > TreeParentIdUtil.ROOT_PARENT_ID && sysDeptMapper.countActiveById(parentId) == 0) {
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
		TreeRelationUtil.requireValidMoveTarget(deptId, newParentId,
				(ancestorId, nodeId) -> deptReferenceMapper.countDescendantRelation(ancestorId, nodeId) > 0);
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
