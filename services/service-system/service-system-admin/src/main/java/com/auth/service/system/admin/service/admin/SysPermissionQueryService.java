package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统权限只读查询。
 *
 * @author Bunny
 */
public interface SysPermissionQueryService extends IService<SysPermissionEntity> {

	/**
	 * 分页查询权限列表
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysPermissionPageVO> getPage(Page<SysPermissionEntity> pageParams, SysPermissionQuery query);

	/**
	 * 权限详情
	 * @param id 权限主键
	 * @return 详情 VO
	 */
	SysPermissionDetailVO getDetail(Long id);

}
