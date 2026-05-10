package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.SysMenuListQuery;
import com.auth.service.system.admin.model.query.SysMenuPageQuery;
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
	 * 条件查询全部菜单（扁平，供前端建树）
	 * @param query 筛选条件
	 * @return 菜单行
	 */
	List<SysMenuListVO> listFlat(SysMenuListQuery query);

	/**
	 * 分页查询菜单（表格视图）
	 * @param pageParams 分页参数
	 * @param query 筛选与分页
	 * @return 分页结果
	 */
	PageResponse<SysMenuListVO> pageFlat(Page<SysMenuEntity> pageParams, SysMenuPageQuery query);

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
	 * 批量逻辑删除
	 * @param ids 主键列表
	 */
	void removeByIds(List<Long> ids);

}
