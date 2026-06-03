package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.admin.model.query.LogUserPasswordHistoryQuery;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryDetailVO;
import com.auth.service.system.admin.model.vo.loguserpasswordhistory.LogUserPasswordHistoryPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 密码历史日志 Service
 *
 * @author Bunny
 */
public interface LogUserPasswordHistoryService extends IService<LogUserPasswordHistoryEntity> {

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

}
