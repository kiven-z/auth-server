package com.auth.service.system.schedule.service;

import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobLastExecutionView;
import com.auth.service.system.schedule.model.vo.SysJobPageResponse;

import java.util.List;

/**
 * 定时任务运行时信息补充（Quartz 快照、触发时间、最近执行日志）
 *
 * @author Bunny
 */
public interface SysJobRuntimeEnrichmentService {

	/**
	 * 为分页行补充最近一次执行结果（批量查 log_job）
	 * @param rows 分页行
	 */
	void enrichPageLastExecution(List<SysJobPageResponse> rows);

	/**
	 * 为详情补充 Quartz 运行态、触发时间与最近执行结果
	 * @param detail 详情 VO
	 * @param jobId 任务主键
	 * @param jobName 任务名
	 * @param jobGroup 任务分组
	 */
	void enrichDetail(SysJobDetailVO detail, Long jobId, String jobName, String jobGroup);

	/**
	 * 任务是否处于执行中或排队中（禁止重复立即执行）
	 * @param jobName 任务名
	 * @param jobGroup 任务分组
	 * @return 是否 busy
	 */
	boolean isBusy(String jobName, String jobGroup);

	/**
	 * 批量写入最近一次执行结果
	 * @param views 可写入最近执行结果的视图
	 */
	void enrichLastExecution(List<? extends SysJobLastExecutionView> views);

}
