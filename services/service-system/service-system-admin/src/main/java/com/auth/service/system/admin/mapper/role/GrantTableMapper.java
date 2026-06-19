package com.auth.service.system.admin.mapper.role;

import com.auth.service.system.admin.model.entity.GrantTableEntity;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundDeptPO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundPostPO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundRolePO;
import com.auth.service.system.admin.model.po.granttable.GrantTableBoundUserPO;
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
	 * 统计指定角色码在 grant_table 中的引用行数。
	 * @param roleCode 角色编码
	 * @return 引用行数
	 */
	@Select("SELECT COUNT(1) FROM grant_table WHERE role_code = #{roleCode}")
	long countByRoleCode(@Param("roleCode") String roleCode);

	/**
	 * 按授权主体删除全部 grant 行。
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @return 删除行数
	 */
	@Delete("DELETE FROM grant_table WHERE subject_type = #{subjectType} AND subject_id = #{subjectId}")
	int deleteBySubject(@Param("subjectType") String subjectType, @Param("subjectId") Long subjectId);

	/**
	 * 按授权主体查询已绑定角色（联表 sys_role，返回 grant_table 中的全部关联）。
	 * @param subjectType 主体类型
	 * @param subjectId 主体 ID
	 * @return 已授权角色投影列表
	 */
	List<GrantTableBoundRolePO> selectBoundRolesBySubject(@Param("subjectType") String subjectType,
			@Param("subjectId") Long subjectId);

	/**
	 * 按角色码反查已授权用户（联表 sys_user，返回 grant_table 中的全部关联）。
	 * @param roleCode 角色编码
	 * @return 已授权用户投影列表
	 */
	List<GrantTableBoundUserPO> selectBoundUsersByRoleCode(@Param("roleCode") String roleCode);

	/**
	 * 按角色码反查已授权部门（联表 sys_dept，返回 grant_table 中的全部关联）。
	 * @param roleCode 角色编码
	 * @return 已授权部门投影列表
	 */
	List<GrantTableBoundDeptPO> selectBoundDeptsByRoleCode(@Param("roleCode") String roleCode);

	/**
	 * 按角色码反查已授权岗位（联表 sys_post，返回 grant_table 中的全部关联）。
	 * @param roleCode 角色编码
	 * @return 已授权岗位投影列表
	 */
	List<GrantTableBoundPostPO> selectBoundPostsByRoleCode(@Param("roleCode") String roleCode);

}
