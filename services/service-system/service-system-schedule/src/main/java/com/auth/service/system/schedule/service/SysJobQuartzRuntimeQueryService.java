package com.auth.service.system.schedule.service;

import com.auth.service.system.schedule.model.vo.SysJobPageResponse;

import java.util.List;

/**
 * Quartz 任务运行时状态查询
 *
 * @author Bunny
 */
public interface SysJobQuartzRuntimeQueryService {

	/**
	 * 为分页行补充 Quartz 运行时状态
	 * @param rows 分页行
	 */
	void enrichPageRows(List<SysJobPageResponse> rows);

}
