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
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 定时任务服务（定义态 CRUD + 运行态启停与立即执行）
 *
 * @author Bunny
 */
public interface SysJobDefinitionService extends IService<SysJobEntity> {

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

	/**
	 * 批量变更状态并同步 Quartz
	 * @param form 任务 ID 列表与目标状态
	 */
	void batchUpdateStatus(SysJobStatusForm form);

	/**
	 * 按分组批量变更任务状态并同步 Quartz（停用走 pauseGroup，启用重建分组内全部调度）
	 * @param groupCode 分组编码
	 * @param status true=恢复调度，false=暂停
	 */
	void batchUpdateStatusByGroupCode(String groupCode, Boolean status);

	/**
	 * 立即执行一次（不影响 Cron）
	 * @param id 主键
	 */
	void runOnce(Long id);

}
