package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统角色写服务。
 *
 * @author Bunny
 */
public interface SysRoleService extends IService<SysRoleEntity> {

	/**
	 * 新增角色
	 * @param form 新增表单
	 */
	void create(SysRoleForm form);

	/**
	 * 更新角色
	 * @param form 保存表单
	 */
	void update(SysRoleForm form);

	/**
	 * 按主键删除角色
	 * @param id 角色主键
	 */
	void deleteById(Long id);

}
