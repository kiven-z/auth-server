package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.query.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统角色服务
 *
 * @author Bunny
 */
public interface SysRoleService extends IService<SysRoleEntity> {

	/**
	 * 分页查询角色列表
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysRolePageResponse> getPage(Page<SysRoleEntity> pageParams, SysRoleQuery query);

	/**
	 * 获取角色详情 包含禁用
	 * @param id 角色ID
	 * @return 角色详情
	 */
	SysRoleDetailVO getDetail(Long id);

	/**
	 * 新增角色
	 * @param form 新增表单
	 */
	void create(SysRoleForm form);

	/**
	 * 更新角色；仅当 {@code role_code} 或 {@code status} 变化时触发授权失效。
	 * @param form 保存表单
	 */
	void update(SysRoleForm form);

	/**
	 * 查询角色下拉选项
	 * @param roleName 角色名称（可选模糊）
	 * @param roleCode 角色编码（可选模糊）
	 * @return 下拉选项列表
	 */
	List<SysRoleOptionVO> listOptions(String roleName, String roleCode);

	/**
	 * 按主键删除角色；grant_table 仍有引用时拒绝删除，成功后触发授权失效。
	 * @param id 角色主键
	 */
	void deleteById(Long id);

}
