package com.auth.service.system.schedule.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.entity.SysJobEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobStatusForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 定时任务与 Quartz 同步
 *
 * @author Bunny
 */
public interface SysJobService {

	/**
	 * 启动时从库全量同步到调度器
	 */
	void syncAllFromDatabase();

	/**
	 * 分页查询（补充 Quartz 触发时间）
	 * @param pageParams 分页
	 * @param query 条件
	 * @return 分页数据
	 */
	PageResponse<SysJobPageResponse> getPage(Page<SysJobEntity> pageParams, SysJobQuery query);

	/**
	 * 详情
	 * @param id 主键
	 * @return 详情
	 */
	SysJobDetailVO getDetail(Long id);

	/**
	 * 新增并调度
	 * @param form 表单
	 */
	void create(SysJobCreateForm form);

	/**
	 * 修改并必要时重建触发器/Job
	 * @param form 表单
	 */
	void update(SysJobUpdateForm form);

	/**
	 * 删除库记录并移除 Quartz Job
	 * @param id 主键
	 */
	void deleteById(Long id);

	/**
	 * 批量变更状态并 pause/resume
	 * @param form 表单
	 */
	void batchUpdateStatus(SysJobStatusForm form);

	/**
	 * 立即执行一次
	 * @param id 主键
	 */
	void runOnce(Long id);

	/**
	 * 按分组暂停（库状态 + Quartz）
	 * @param groupCode 分组编码
	 */
	void pauseAllInGroup(String groupCode);

	/**
	 * 按分组恢复（库状态 + Quartz）
	 * @param groupCode 分组编码
	 */
	void resumeAllInGroup(String groupCode);

}
