package com.auth.service.system.log.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.entity.JobLogEntity;
import com.auth.service.system.schedule.model.query.SysJobLogQuery;
import com.auth.service.system.schedule.model.vo.SysJobLogDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobLogPageResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 定时任务日志服务
 *
 * @author Bunny
 */
public interface LogJobService extends IService<JobLogEntity> {

	/**
	 * 分页查询
	 * @param pageParams 分页
	 * @param query 条件
	 * @return 分页数据
	 */
	PageResponse<SysJobLogPageResponse> getPage(Page<JobLogEntity> pageParams, SysJobLogQuery query);

	/**
	 * 详情（含异常堆栈）
	 * @param id 主键
	 * @return 详情
	 */
	SysJobLogDetailVO getDetail(Long id);

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
