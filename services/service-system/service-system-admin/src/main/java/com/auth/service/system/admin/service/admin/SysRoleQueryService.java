package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统角色只读查询。
 *
 * @author Bunny
 */
public interface SysRoleQueryService extends IService<SysRoleEntity> {

	/**
	 * 分页查询角色列表
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysRolePageVO> getPage(Page<SysRoleEntity> pageParams, SysRoleQuery query);

	/**
	 * 获取角色详情（含禁用）
	 * @param id 角色 ID
	 * @return 角色详情
	 */
	SysRoleDetailVO getDetail(Long id);

	/**
	 * 查询角色下拉选项
	 * @param roleName 角色名称（可选模糊）
	 * @param roleCode 角色编码（可选模糊）
	 * @return 下拉选项列表
	 */
	List<SysRoleOptionVO> listOptions(String roleName, String roleCode);

}
