package com.auth.service.system.admin.mapper;

import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.query.SysPermissionQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统权限 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermissionEntity> {

	/**
	 * 分页查询权限（含创建人、更新人用户名展示列）
	 * @param page 分页参数
	 * @param query 筛选条件
	 * @param orderBySql 已由白名单校验的 ORDER BY 片段（仅列名与 ASC/DESC）
	 * @return 分页结果
	 */
	IPage<SysPermissionEntity> selectListByPage(@Param("page") Page<SysPermissionEntity> page,
			@Param("query") SysPermissionQuery query, @Param("orderBySql") String orderBySql);

}
