package com.auth.service.system.admin.role.mapper;

import com.auth.service.system.admin.role.model.entity.SysRoleEntity;
import com.auth.service.system.admin.role.model.query.SysRoleQuery;
import com.auth.service.system.admin.role.model.vo.SysRoleOptionVO;
import com.auth.service.system.admin.role.model.vo.SysRolePageResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
	 * 批量自增用户权限版本号（perm_version + 1）
	 * @param userIds 用户 ID 列表
	 */
	void batchIncrementPermVersion(@Param("userIds") List<Long> userIds);

	/**
	 * 统计 grant_table 中对指定 role_code 的引用数
	 * <p>
	 * service-system 无 GrantEntity，使用原生 SQL 查询
	 * </p>
	 * @param roleCode 角色编码
	 * @return 引用行数
	 */
	Integer countGrantTableRefByRoleCode(@Param("roleCode") String roleCode);

	/**
	 * 查询角色下拉选项
	 * @param roleName 角色名称（可选模糊）
	 * @param roleCode 角色编码（可选模糊）
	 * @return 角色选项列表
	 */
	List<SysRoleOptionVO> selectRoleOptions(@Param("roleName") String roleName, @Param("roleCode") String roleCode);

}
