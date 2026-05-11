package com.auth.service.system.log.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.log.model.entity.LogLoginEntity;
import com.auth.service.system.log.model.query.LogLoginLogQuery;
import com.auth.service.system.log.model.vo.LogLoginLogDetailVO;
import com.auth.service.system.log.model.vo.LogLoginLogPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 登录日志 Service
 *
 * @author Bunny
 */
public interface LogLoginService extends IService<LogLoginEntity> {

	/**
	 * 分页查询登录日志
	 * @param pageParams 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<LogLoginLogPageVO> getPage(Page<LogLoginEntity> pageParams, LogLoginLogQuery query);

	/**
	 * 登录日志详情
	 * @param id 主键
	 * @return 详情
	 */
	LogLoginLogDetailVO getDetail(Long id);

}
