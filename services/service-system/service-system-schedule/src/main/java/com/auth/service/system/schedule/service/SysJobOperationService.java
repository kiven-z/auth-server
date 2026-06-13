package com.auth.service.system.schedule.service;

import com.auth.service.system.schedule.model.form.SysJobStatusForm;

/**
 * 定时任务操作服务（运行态：启停、立即执行、启动同步）
 *
 * @author Bunny
 */
public interface SysJobOperationService {

	/**
	 * 启动时从库全量同步到调度器
	 */
	void syncAllFromDatabase();

	/**
	 * 批量变更状态并
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
