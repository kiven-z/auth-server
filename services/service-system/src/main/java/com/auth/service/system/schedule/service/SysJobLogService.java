package com.auth.service.system.schedule.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.entity.SysJobLogEntity;
import com.auth.service.system.schedule.model.query.SysJobLogQuery;
import com.auth.service.system.schedule.model.vo.SysJobLogDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobLogPageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * 定时任务执行日志
 *
 * @author Bunny
 */
public interface SysJobLogService {

	/**
	 * 分页查询
	 * @param pageParams 分页
	 * @param query 条件
	 * @return 分页数据
	 */
	PageResponse<SysJobLogPageResponse> getPage(Page<SysJobLogEntity> pageParams, SysJobLogQuery query);

	/**
	 * 详情（含异常堆栈）
	 * @param id 主键
	 * @return 详情
	 */
	SysJobLogDetailVO getDetail(Long id);

	/**
	 * 记录成功日志（由 Quartz 监听调用）
	 * @param jobId 任务ID
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 * @param invokeTarget 任务执行目标
	 * @param elapsedMs 执行时间
	 * @param message 消息
	 */
	void recordSuccess(Long jobId, String jobName, String jobGroup, String invokeTarget, long elapsedMs,
			String message);

	/**
	 * 记录失败日志（由 Quartz 监听调用）
	 * @param jobId 任务ID
	 * @param jobName 任务名称
	 * @param jobGroup 任务组
	 * @param invokeTarget 任务执行目标
	 * @param elapsedMs 执行时间
	 * @param ex 异常
	 */
	void recordFailure(Long jobId, String jobName, String jobGroup, String invokeTarget, long elapsedMs,
			JobExecutionException ex);

	/**
	 * 批量删除
	 * @param ids 主键
	 */
	void removeBatch(List<Long> ids);

	/**
	 * 发送清空日志验证码
	 */
	void sendClearLogVerificationCode();

	/**
	 * 清空全部日志
	 * @param verificationCode 验证码
	 */
	void clearAll(String verificationCode);

}
