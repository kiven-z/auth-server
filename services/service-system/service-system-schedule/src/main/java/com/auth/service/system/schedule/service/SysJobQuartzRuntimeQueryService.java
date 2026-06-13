package com.auth.service.system.schedule.service;

import com.auth.service.system.schedule.model.vo.SysJobPageResponse;
import com.auth.service.system.schedule.model.vo.SysJobQuartzRuntimeStatusVO;

import java.util.List;

/**
 * Quartz 任务运行时状态查询
 *
 * @author Bunny
 */
public interface SysJobQuartzRuntimeQueryService {

	/**
	 * 按任务名称与分组查询 Quartz 运行时状态
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 * @return 运行时状态
	 */
	SysJobQuartzRuntimeStatusVO getRuntimeStatus(String jobName, String jobGroup);

	/**
	 * 为分页行补充 Quartz 运行时状态
	 * @param rows 分页行
	 */
	void enrichPageRows(List<SysJobPageResponse> rows);

}
