package com.auth.service.system.admin.role.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.role.model.entity.SysRoleEntity;
import com.auth.service.system.admin.role.model.query.SysRoleQuery;
import com.auth.service.system.admin.role.model.vo.SysRolePageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 系统角色服务
 *
 * @author Bunny
 */
public interface SysRoleService {

	/**
	 * 分页查询角色列表
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysRolePageResponse> getPage(Page<SysRoleEntity> pageParams, SysRoleQuery query);

}
