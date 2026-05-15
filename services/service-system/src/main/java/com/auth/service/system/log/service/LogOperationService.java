package com.auth.service.system.log.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.auth.service.system.log.model.query.LogOperationQuery;
import com.auth.service.system.log.model.vo.LogOperationDetailVO;
import com.auth.service.system.log.model.vo.LogOperationPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 审计管道与内部上报写入；对外提供分页、详情与删除
 *
 * @author Bunny
 */
public interface LogOperationService extends IService<LogOperationEntity> {

	/**
	 * 分页查询操作日志
	 * @param pageParams 分页参数
	 * @param query 筛选条件
	 * @return 分页数据
	 */
	PageResponse<LogOperationPageVO> getPage(Page<LogOperationEntity> pageParams, LogOperationQuery query);

	/**
	 * 操作日志详情
	 * @param id 主键
	 * @return 详情
	 */
	LogOperationDetailVO getDetail(Long id);

}
