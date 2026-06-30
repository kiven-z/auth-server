package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.mapper.dept.DeptClosureMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * 闭包表维护入口：部门树结构变更时调用存储过程
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class DeptClosureMaintainer {

	private final DeptClosureMapper deptClosureMapper;

	/**
	 * 新增部门后写入闭包路径
	 * @param deptId 部门 ID
	 * @param parentId 父部门 ID
	 */
	public void insertNode(long deptId, long parentId) {
		deptClosureMapper.callInsert(deptId, parentId);
	}

	/**
	 * 物理删除该部门,物理删除子树
	 * @param deptId 部门 ID
	 */
	public void removeClosurePaths(long deptId) {
		deptClosureMapper.callDelete(deptId);
	}

	/**
	 * 变更父部门后重算闭包
	 * @param deptId 被移动部门 ID
	 * @param newParentId 新父部门 ID
	 */
	public void moveNode(long deptId, long newParentId) {
		deptClosureMapper.callMove(deptId, newParentId);
	}

}
