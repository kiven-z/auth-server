package com.auth.service.system.schedule.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.model.query.SysJobLogQuery;
import com.auth.service.system.schedule.model.vo.SysJobLogDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobLogPageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.quartz.JobExecutionException;

/**
 * 定时任务执行日志
 *
 * @author Bunny
 */
public interface SysJobLogService extends IService<LogJobEntity> {

	/**
	 * 分页查询
	 * @param pageParams 分页
	 * @param query 条件
	 * @return 分页数据
	 */
	PageResponse<SysJobLogPageResponse> getPage(Page<LogJobEntity> pageParams, SysJobLogQuery query);

	/**
	 * 详情（含异常堆栈）
	 * @param id 主键
	 * @return 详情
	 */
	SysJobLogDetailVO getDetail(Long id);

	/**
	 * 记录成功日志
	 * @param jobId 任务ID
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 * @param invokeTarget 任务执行目标
	 * @param triggerType 触发类型
	 * @param elapsedMs 执行时间
	 * @param message 消息
	 */
	void recordSuccess(Long jobId, String jobName, String jobGroup, String invokeTarget, String triggerType,
			long elapsedMs, String message);

	/**
	 * 记录失败日志
	 * @param jobId 任务ID
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 * @param invokeTarget 任务执行目标
	 * @param triggerType 触发类型
	 * @param elapsedMs 执行时间
	 * @param ex 异常
	 */
	void recordFailure(Long jobId, String jobName, String jobGroup, String invokeTarget, String triggerType,
			long elapsedMs, JobExecutionException ex);

}
