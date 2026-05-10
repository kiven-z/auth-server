package com.auth.service.system.admin.service;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignPermissionForm;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedPermissionRow;
import com.auth.service.system.admin.model.vo.menu.SysMenuAssignedRoleRow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 菜单管理（列表 / 分页 / CRUD / 分配角色与权限）
 *
 * @author Bunny
 */
public interface SysMenuManageService extends IService<SysMenuEntity> {

	/**
	 * 批量更新状态（0=启用 1=禁用）
	 * @param form 请求体
	 */
	void batchUpdateStatus(SysMenuBatchStatusForm form);

	/**
	 * 当前菜单已分配角色（供前端与角色分页合并展示）
	 * @param menuId 菜单主键
	 * @return 已分配行，{@code assigned} 恒为 true
	 */
	List<SysMenuAssignedRoleRow> listAssignedRoles(Long menuId);

	/**
	 * 覆盖写入菜单-角色关联
	 * @param menuId 菜单主键
	 * @param form 角色编码列表
	 */
	void replaceMenuRoles(Long menuId, SysMenuAssignRoleForm form);

	/**
	 * 全量权限列表并标记当前菜单是否已关联（供分配抽屉）
	 * @param menuId 菜单主键
	 * @return 权限行
	 */
	List<SysMenuAssignedPermissionRow> listPermissionsWithAssignFlags(Long menuId);

	/**
	 * 覆盖写入菜单-权限关联
	 * @param menuId 菜单主键
	 * @param form 权限编码列表
	 */
	void replaceMenuPermissions(Long menuId, SysMenuAssignPermissionForm form);

}
