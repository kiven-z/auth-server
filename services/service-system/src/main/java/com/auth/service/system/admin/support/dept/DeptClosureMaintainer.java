package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.mapper.DeptClosureMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 闭包表维护入口：仅树结构变更时调用存储过程。
 *
 * <p>
 * 改部门名称/编码/排序/状态勿调用本类；逻辑删除勿调用 {@link #deleteSubtree}。
 * </p>
 */
@RequiredArgsConstructor
@Component
public class DeptClosureMaintainer {

	private final DeptClosureMapper deptClosureMapper;

	/**
	 * 新增部门后写入闭包路径。
	 * @param deptId 部门 ID
	 * @param parentId 父部门 ID
	 */
	public void insertNode(long deptId, long parentId) {
		deptClosureMapper.callInsert(deptId, parentId);
	}

	/**
	 * 物理删除子树时移除闭包路径。
	 * @param deptId 子树根 ID
	 */
	public void deleteSubtree(long deptId) {
		deptClosureMapper.callDeleteSubtree(deptId);
	}

	/**
	 * 变更父部门后重算闭包（调用前须已更新 {@code sys_dept.parent_id}）。
	 * @param deptId 被移动部门 ID
	 * @param newParentId 新父部门 ID
	 */
	public void moveNode(long deptId, long newParentId) {
		deptClosureMapper.callMove(deptId, newParentId);
	}

}
