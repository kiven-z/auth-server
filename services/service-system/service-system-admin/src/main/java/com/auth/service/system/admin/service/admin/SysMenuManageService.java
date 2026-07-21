package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.vo.reference.ext.MenuAssignedRoleReferenceVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 菜单管理
 *
 * @author Bunny
 */
public interface SysMenuManageService extends IService<SysMenuEntity> {

	/**
	 * 当前菜单已分配角色
	 * @param menuId 菜单主键
	 * @return 已分配行
	 */
	List<MenuAssignedRoleReferenceVO> listAssignedRoles(Long menuId);

	/**
	 * 覆盖写入菜单-角色关联
	 * @param menuId 菜单主键
	 * @param form 角色 ID 列表
	 */
	void replaceMenuRoles(Long menuId, SysMenuAssignRoleForm form);

}
