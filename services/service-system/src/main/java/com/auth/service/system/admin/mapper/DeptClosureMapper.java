package com.auth.service.system.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 部门闭包表写操作
 *
 * @author Bunny
 */
@Mapper
public interface DeptClosureMapper {

	/**
	 * 新增节点后写入闭包
	 * @param deptId 新部门主键
	 * @param parentId 父部门 ID，顶级传
	 */
	void callInsert(@Param("deptId") Long deptId, @Param("parentId") Long parentId);

	/**
	 * 物理删除子树在闭包表中的全部路径
	 * @param deptId 子树根节点 ID
	 */
	void callDeleteSubtree(@Param("deptId") Long deptId);

	/**
	 * 变更父部门后重算闭包路径（须已更新 sys_dept.parent_id）
	 * @param deptId 被移动部门 ID
	 * @param newParentId 新父部门 ID，顶级传 0
	 */
	void callMove(@Param("deptId") Long deptId, @Param("newParentId") Long newParentId);

	/**
	 * 逻辑删除部门后移除闭包路径（仅叶子节点场景）
	 * @param deptId 被逻辑删除的部门 ID
	 */
	void callRemoveLogical(@Param("deptId") Long deptId);

}
