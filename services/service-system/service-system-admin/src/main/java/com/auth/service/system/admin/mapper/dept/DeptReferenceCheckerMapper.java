package com.auth.service.system.admin.mapper.dept;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 部门删除/移动前的引用与树关系只读查询
 *
 * @author Bunny
 */
@Mapper
public interface DeptReferenceCheckerMapper {

	/**
	 * 子树内是否存在用户-部门关联（含子树任意节点上的挂靠）
	 * @param deptId 子树根部门 ID
	 * @return 引用行数
	 */
	long countUserDeptInSubtree(@Param("deptId") Long deptId);

	/**
	 * 子树内是否存在 grant_table 的 DEPT 主体引用
	 * @param deptId 子树根部门 ID
	 * @return 引用行数
	 */
	long countGrantDeptSubjectInSubtree(@Param("deptId") Long deptId);

	/**
	 * 子树内是否存在未删除岗位
	 * @param deptId 子树根部门 ID
	 * @return 引用行数
	 */
	long countPostInSubtree(@Param("deptId") Long deptId);

	/**
	 * {@code descendantId} 是否位于 {@code ancestorId} 的子树中（含自身）
	 * @param ancestorId 祖先部门 ID
	 * @param descendantId 候选后代部门 ID
	 * @return 匹配行数，大于 0 表示是后代
	 */
	long countDescendantRelation(@Param("ancestorId") Long ancestorId, @Param("descendantId") Long descendantId);

}
