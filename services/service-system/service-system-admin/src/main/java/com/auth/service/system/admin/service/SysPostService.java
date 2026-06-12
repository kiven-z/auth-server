package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageResponse;
import com.auth.service.system.admin.model.vo.post.SysPostSearchItemResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;


/**
 * 系统岗位服务
 *
 * @author Bunny
 */
public interface SysPostService {

	/**
	 * 分页查询岗位
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysPostPageResponse> getPage(Page<SysPostEntity> pageParams, SysPostQuery query);

	/**
	 * 岗位关键词搜索（编码/名称前缀匹配）
	 * @param keyword 搜索关键字（前缀匹配，可选）
	 * @param status 启用状态（可选）
	 * @param limit 返回条数上限，默认 20，最大 50
	 * @return 搜索项列表
	 */
	List<SysPostSearchItemResponse> search(String keyword, Boolean status, Integer limit);

	/**
	 * 岗位详情（含所属部门、关联用户、已授权角色）
	 * @param id 岗位主键
	 * @return 详情
	 */
	SysPostDetailVO getDetail(Long id);

	/**
	 * 新增岗位
	 * @param form 新增表单
	 */
	void create(SysPostForm form);

	/**
	 * 更新岗位
	 * @param id 岗位主键
	 * @param form 表单（与新增字段一致；所属部门不可变更）
	 */
	void update(Long id, SysPostForm form);

	/**
	 * 按主键逻辑删除岗位；user_post 或 grant_table 仍有引用时拒绝删除，成功后触发授权失效。
	 * @param id 岗位主键
	 */
	void deleteById(Long id);

	/**
	 * 批量导入岗位（单事务，任意行失败全部回滚）
	 * @param forms 已校验并转换好的表单列表
	 */
	void importBatch(List<SysPostForm> forms);

}
