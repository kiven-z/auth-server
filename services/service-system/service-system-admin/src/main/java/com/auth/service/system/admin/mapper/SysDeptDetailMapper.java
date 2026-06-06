package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.po.dept.SysDeptBoundPostPO;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundUserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门详情关联数据只读查询
 *
 * @author bunny
 */
@Mapper
public interface SysDeptDetailMapper {

	/**
	 * 查询直接挂靠本部门的有效用户-部门关联（不含子部门）
	 * @param deptId 部门 ID
	 * @return 关联用户列表
	 */
	List<SysDeptBoundUserPO> selectBoundUsersByDeptId(@Param("deptId") Long deptId);

	/**
	 * 查询本部门下未删除岗位（不含子部门）
	 * @param deptId 部门 ID
	 * @return 岗位列表
	 */
	List<SysDeptBoundPostPO> selectBoundPostsByDeptId(@Param("deptId") Long deptId);

}
