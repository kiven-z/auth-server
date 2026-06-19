package com.auth.service.system.admin.service.admin;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageVO;
import com.auth.service.system.admin.model.vo.post.SysPostSearchItemVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统岗位只读查询。
 *
 * @author Bunny
 */
public interface SysPostQueryService extends IService<SysPostEntity> {

	/**
	 * 分页查询岗位
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysPostPageVO> getPage(Page<SysPostEntity> pageParams, SysPostQuery query);

	/**
	 * 岗位关键词搜索（编码/名称前缀匹配）
	 * @param keyword 搜索关键字（前缀匹配，可选）
	 * @param status 启用状态（可选）
	 * @param limit 返回条数上限，默认 20，最大 50
	 * @return 搜索项列表
	 */
	List<SysPostSearchItemVO> search(String keyword, Boolean status, Integer limit);

	/**
	 * 岗位详情（含所属部门、关联用户、已授权角色）
	 * @param id 岗位主键
	 * @return 详情
	 */
	SysPostDetailVO getDetail(Long id);

}
