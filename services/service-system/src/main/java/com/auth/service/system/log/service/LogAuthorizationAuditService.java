package com.auth.service.system.log.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.log.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.log.model.query.LogAuthorizationAuditQuery;
import com.auth.service.system.log.model.vo.LogAuthorizationAuditDetailVO;
import com.auth.service.system.log.model.vo.LogAuthorizationAuditPageVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 权限决策审计日志 Service
 *
 * @author Bunny
 */
public interface LogAuthorizationAuditService extends IService<LogAuthorizationAuditEntity> {

	/**
	 * 分页查询
	 * @param pageParams 分页
	 * @param query 条件
	 * @return 分页数据
	 */
	PageResponse<LogAuthorizationAuditPageVO> getPage(Page<LogAuthorizationAuditEntity> pageParams,
			LogAuthorizationAuditQuery query);

	/**
	 * 详情
	 * @param id 主键
	 * @return 详情
	 */
	LogAuthorizationAuditDetailVO getDetail(Long id);

}
