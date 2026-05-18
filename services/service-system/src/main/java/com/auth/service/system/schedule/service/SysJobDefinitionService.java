package com.auth.service.system.schedule.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 定时任务定义服务（定义态：增删改查）
 *
 * @author Bunny
 */
public interface SysJobDefinitionService {

	/**
	 * 分页查询
	 * @param pageParams 分页
	 * @param query 查询参数
	 * @return 分页结果
	 */
	PageResponse<SysJobPageResponse> getPage(Page<SysJobEntity> pageParams, SysJobQuery query);

	/**
	 * 详情查询
	 * @param id 主键
	 * @return 详情
	 */
	SysJobDetailVO getDetail(Long id);

	/**
	 * 新增任务定义并注册调度
	 * @param form 新增表单
	 */
	void create(SysJobCreateForm form);

	/**
	 * 更新任务定义并重建调度
	 * @param form 更新表单
	 */
	void update(SysJobUpdateForm form);

	/**
	 * 删除任务定义并移除调度
	 * @param id 主键
	 */
	void deleteById(Long id);

}
