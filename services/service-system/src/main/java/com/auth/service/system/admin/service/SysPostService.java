package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostCreateForm;
import com.auth.service.system.admin.model.query.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostPageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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
	 * 新增岗位
	 * @param form 新增表单
	 */
	void create(SysPostCreateForm form);

}
