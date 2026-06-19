package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.form.menu.SysMenuMoveForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 菜单写服务。
 *
 * @author Bunny
 */
public interface SysMenuService extends IService<SysMenuEntity> {

	/**
	 * 新增菜单
	 * @param form 表单
	 * @return 新菜单主键字符串（与前端约定）
	 */
	Long create(SysMenuSaveForm form);

	/**
	 * 更新菜单
	 * @param form 表单（含 id）
	 */
	void update(SysMenuSaveForm form);

	/**
	 * 移动菜单（变更父菜单）
	 * @param menuId 菜单主键
	 * @param form 新父菜单
	 */
	void move(Long menuId, SysMenuMoveForm form);

	/**
	 * 批量更新状态（0=启用 1=禁用）
	 * @param form 请求体
	 */
	void batchUpdateStatus(SysMenuBatchStatusForm form);

	/**
	 * 批量物理删除菜单（先清理关联表，再删主表；存在子菜单时拒绝）。
	 * @param ids 菜单主键列表
	 */
	void deleteByIds(List<Long> ids);

}
