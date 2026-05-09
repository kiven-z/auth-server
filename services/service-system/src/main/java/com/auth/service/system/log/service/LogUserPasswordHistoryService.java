package com.auth.service.system.log.service;

import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.log.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.log.model.query.LogUserPasswordHistoryQuery;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.log.model.vo.LogUserPasswordHistoryPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 密码历史日志 Service
 *
 * @author Bunny
 */
public interface LogUserPasswordHistoryService {

	/**
	 * 分页查询密码历史日志
	 * @param pageParams 分页
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<LogUserPasswordHistoryPageVO> getPage(Page<LogUserPasswordHistoryEntity> pageParams,
			LogUserPasswordHistoryQuery query);

	/**
	 * 详情
	 * @param id 主键
	 * @return 详情
	 */
	LogUserPasswordHistoryDetailVO getDetail(Long id);

	/**
	 * 批量删除
	 * @param ids 主键集合
	 */
	void removeBatch(List<Long> ids);

}
