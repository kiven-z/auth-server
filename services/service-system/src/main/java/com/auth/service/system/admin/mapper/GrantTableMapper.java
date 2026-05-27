package com.auth.service.system.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * grant_table 只读查询（角色删除前引用检查）。
 *
 * @author Bunny
 */
@Mapper
public interface GrantTableMapper {

	/**
	 * 统计指定角色码在 grant_table 中的引用行数。
	 * @param roleCode 角色编码
	 * @return 引用行数
	 */
	long countByRoleCode(@Param("roleCode") String roleCode);

}
