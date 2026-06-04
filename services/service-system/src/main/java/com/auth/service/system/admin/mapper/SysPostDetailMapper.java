package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.po.dept.SysDeptBoundRolePO;
import com.auth.service.system.admin.model.po.post.SysPostBoundDeptPO;
import com.auth.service.system.admin.model.po.post.SysPostBoundUserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位详情关联数据只读查询
 *
 * @author Bunny
 */
@Mapper
public interface SysPostDetailMapper {

	/**
	 * 查询岗位所属部门快照
	 * @param postId 岗位 ID
	 * @return 部门快照，岗位或部门不存在时为 null
	 */
	SysPostBoundDeptPO selectBoundDeptByPostId(@Param("postId") Long postId);

	/**
	 * 查询岗位下有效用户-岗位关联
	 * @param postId 岗位 ID
	 * @return 关联用户列表
	 */
	List<SysPostBoundUserPO> selectBoundUsersByPostId(@Param("postId") Long postId);

	/**
	 * 查询以本岗位为授权主体（POST）的角色授权边
	 * @param postId 岗位 ID
	 * @return 已授权角色列表
	 */
	List<SysDeptBoundRolePO> selectBoundRolesByPostId(@Param("postId") Long postId);

}
