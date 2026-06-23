package com.auth.service.system.admin.mapper.post;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.auth.service.system.admin.model.po.post.SysPostBoundUserPO;
import com.auth.service.system.admin.model.po.reference.DeptReferencePO;

/**
 * 岗位详情关联数据只读查询
 *
 * @author Bunny
 */
@Mapper
public interface SysPostQueryMapper {

	/**
	 * 查询岗位所属部门快照
	 * @param postId 岗位 ID
	 * @return 部门快照，岗位或部门不存在时为 null
	 */
	DeptReferencePO selectBoundDeptByPostId(@Param("postId") Long postId);

	/**
	 * 查询岗位下有效用户-岗位关联
	 * @param postId 岗位 ID
	 * @return 关联用户列表
	 */
	List<SysPostBoundUserPO> selectBoundUsersByPostId(@Param("postId") Long postId);

}
