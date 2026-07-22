package com.auth.service.system.schedule.service;

import com.auth.common.core.model.form.IdsEnableStatusForm;

/**
 * 定时任务运行态同步（批量启停、按分组启停）
 *
 * @author Bunny
 */
public interface SysJobScheduleSyncService {

	/**
	 * 按任务 ID 批量变更状态并同步 Quartz
	 * @param form 任务 ID 列表与目标状态
	 */
	void batchUpdateStatus(IdsEnableStatusForm form);

	/**
	 * 按分组批量变更任务状态并同步 Quartz（停用走 pauseGroup，启用重建分组内全部调度）
	 * @param groupCode 分组编码
	 * @param status true=恢复调度，false=暂停
	 */
	void batchUpdateStatusByGroupCode(String groupCode, Boolean status);

}
