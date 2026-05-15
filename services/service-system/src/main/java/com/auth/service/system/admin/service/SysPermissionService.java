package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionCreateForm;
import com.auth.service.system.admin.model.query.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 系统权限服务
 *
 * @author Bunny
 */
public interface SysPermissionService {

	/**
	 * 分页查询权限列表
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysPermissionPageResponse> getPage(Page<SysPermissionEntity> pageParams, SysPermissionQuery query);

	/**
	 * 新增权限
	 * @param form 新增表单
	 */
	void create(SysPermissionCreateForm form);

}
