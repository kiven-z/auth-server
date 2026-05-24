package com.auth.service.system.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 统一授权表 {@code grant_table} 查询（角色删除前引用检查）。
 *
 * @author Bunny
 */
@Mapper
public interface GrantTableMapper {

	/**
	 * 统计持有指定角色的授权边数量。
	 * @param roleCode 角色编码
	 * @return 引用条数
	 */
	long countByRoleCode(@Param("roleCode") String roleCode);

}
