package com.auth.service.system.admin.mapper.role;

import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.po.reference.DeptReferencePO;
import com.auth.service.system.admin.model.po.reference.PostReferencePO;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.po.reference.UserReferencePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * grant_table 读写
 *
 * @author Bunny
 */
@Mapper
public interface GrantTableMapper extends BaseMapper<GrantTableEntity> {

	/**
	 * 统计指定角色在 grant_table 中的引用行数
	 * @param roleId 角色 ID
	 * @return 引用行数
	 */
	@Select("SELECT COUNT(1) FROM grant_table WHERE role_id = #{roleId}")
	long countByRoleId(@Param("roleId") Long roleId);

	/**
	 * 按授权主体删除全部 grant 行
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 */
	@Delete("DELETE FROM grant_table WHERE subject_type = #{subjectType} AND subject_id = #{subjectId}")
	void deleteBySubject(@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId);

	/**
	 * 按授权主体查询已绑定角色
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @return 已授权角色投影列表
	 */
	List<RoleReferencePO> selectBoundRolesBySubject(@Param("subjectType") String subjectType,
			@Param("subjectId") Long subjectId);

	/**
	 * 按角色 ID 反查已授权用户
	 * @param roleId 角色 ID
	 * @return 已授权用户投影列表
	 */
	List<UserReferencePO> selectBoundUsersByRoleId(@Param("roleId") Long roleId);

	/**
	 * 按角色 ID 反查已授权部门
	 * @param roleId 角色 ID
	 * @return 已授权部门投影列表
	 */
	List<DeptReferencePO> selectBoundDeptsByRoleId(@Param("roleId") Long roleId);

	/**
	 * 按角色 ID 反查已授权岗位
	 * @param roleId 角色 ID
	 * @return 已授权岗位投影列表
	 */
	List<PostReferencePO> selectBoundPostsByRoleId(@Param("roleId") Long roleId);

}
