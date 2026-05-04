package com.auth.service.system.admin.role.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.auth.service.system.admin.role.model.entity.SysRoleEntity;
import com.auth.service.system.admin.role.model.query.SysRoleQuery;
import com.auth.service.system.admin.role.model.vo.SysRolePageResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 系统角色 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {

	/**
	 * 分页查询角色（含创建人、更新人用户名展示列）
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @param orderBySql 已由白名单校验的 ORDER BY 片段（仅列名与 ASC/DESC）
	 * @return 分页结果
	 */
	IPage<SysRolePageResponse> selectListByPage(@Param("page") Page<SysRoleEntity> page,
			@Param("query") SysRoleQuery query, @Param("orderBySql") String orderBySql);

	/**
	 * 级联更新所有关联表的 role_code
	 * @param oldRoleCode 旧角色编码
	 * @param newRoleCode 新角色编码
	 */
	void cascadeUpdateRoleCode(@Param("oldRoleCode") String oldRoleCode,
			@Param("newRoleCode") String newRoleCode);

	/**
	 * 统计 grant_table 中对指定 role_code 的引用数
	 * @param roleCode 角色编码
	 * @return 引用行数
	 */
	Integer countGrantTableRefByRoleCode(@Param("roleCode") String roleCode);

	/**
	 * 逻辑删除单条角色
	 * @param id 角色ID
	 */
	void logicDeleteById(@Param("id") Long id);

	/**
	 * 批量逻辑删除角色
	 * @param ids 角色ID列表
	 */
	void logicDeleteBatchByIds(@Param("ids") List<Long> ids);

	/**
	 * 更新角色状态
	 * @param id 角色ID
	 * @param status 状态
	 */
	void updateStatusById(@Param("id") Long id, @Param("status") Integer status);

	/**
	 * 批量自增用户权限版本号（perm_version + 1）
	 * @param userIds 用户 ID 列表
	 */
	void batchIncrementPermVersion(@Param("userIds") List<Long> userIds);

}
