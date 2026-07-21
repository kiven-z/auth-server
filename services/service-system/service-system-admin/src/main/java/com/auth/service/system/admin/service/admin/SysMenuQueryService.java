package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.query.menu.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 菜单只读查询。
 *
 * @author Bunny
 */
public interface SysMenuQueryService extends IService<SysMenuEntity> {

	/**
	 * 查询指定用户可见的 Web 动态路由列表
	 * @param userId 当前用户 ID，null 时返回空列表
	 * @return 顶级路由节点，可能为空
	 */
	List<RouteNodeVO> listWebRoutes(Long userId);

	/**
	 * 条件查询全部菜单
	 * @param query 筛选条件
	 * @return 菜单行
	 */
	List<SysMenuListVO> listFlat(SysMenuQuery query);

	/**
	 * 分页查询菜单
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

}
