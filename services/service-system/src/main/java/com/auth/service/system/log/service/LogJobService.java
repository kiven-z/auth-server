package com.auth.service.system.log.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.log.model.entity.JobLogEntity;
import com.auth.service.system.log.model.query.LogJobQuery;
import com.auth.service.system.log.model.vo.LogJobDetailVO;
import com.auth.service.system.log.model.vo.LogJobPageResponse;
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
	PageResponse<LogJobPageResponse> getPage(Page<JobLogEntity> pageParams, LogJobQuery query);

	/**
	 * 详情（含异常堆栈）
	 * @param id 主键
	 * @return 详情
	 */
	LogJobDetailVO getDetail(Long id);

}
