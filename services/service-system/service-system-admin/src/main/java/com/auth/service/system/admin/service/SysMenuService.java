package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuBatchStatusForm;
import com.auth.service.system.admin.model.form.menu.SysMenuMoveForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 菜单服务
 *
 * @author Bunny
 */
public interface SysMenuService extends IService<SysMenuEntity> {

	/**
	 * 查询指定用户可见的 Web 动态路由列表（顶级节点列表）
	 * <p>
	 * 内部通过 Feign 拉取 auth 生效编码后再建树（建树逻辑迭代中）
	 * </p>
	 * @param userId 当前用户 ID，null 时返回空列表
	 * @return 顶级路由节点，可能为空
	 */
	List<RouteNodeVO> listWebRoutes(Long userId);

	/**
	 * 条件查询全部菜单（扁平，供前端建树）
	 * @param query 筛选条件
	 * @return 菜单行
	 */
	List<SysMenuListVO> listFlat(SysMenuQuery query);

	/**
	 * 分页查询菜单（表格视图）
	 * @param pageParams 分页参数
	 * @param query 筛选与分页
	 * @return 分页结果
	 */
	PageResponse<SysMenuListVO> pageFlat(Page<SysMenuEntity> pageParams, SysMenuQuery query);

	/**
	 * 菜单详情
	 * @param id 主键
	 * @return 详情
	 */
	SysMenuDetailVO getDetail(Long id);

	/**
	 * 新增菜单
	 * @param form 表单
	 * @return 新菜单主键字符串（与前端约定）
	 */
	String create(SysMenuSaveForm form);

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
