package com.auth.service.system.admin.service.admin;

import java.util.List;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.vo.reference.ext.MenuAssignedRoleReferenceVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 菜单管理（列表 / 分页 / CRUD / 分配角色）
 *
 * @author Bunny
 */
public interface SysMenuManageService extends IService<SysMenuEntity> {

	/**
	 * 当前菜单已分配角色（供前端与角色分页合并展示） 不包含禁用
	 * @param menuId 菜单主键
	 * @return 已分配行，{@code assigned} 恒为 true
	 */
	List<MenuAssignedRoleReferenceVO> listAssignedRoles(Long menuId);

	/**
	 * 覆盖写入菜单-角色关联
	 * @param menuId 菜单主键
	 * @param form 角色 ID 列表
	 */
	void replaceMenuRoles(Long menuId, SysMenuAssignRoleForm form);

}
