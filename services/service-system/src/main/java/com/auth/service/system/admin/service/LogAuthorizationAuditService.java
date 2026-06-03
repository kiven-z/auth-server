package com.auth.service.system.admin.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.query.LogAuthorizationAuditQuery;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditDetailVO;
import com.auth.service.system.admin.model.vo.logauthorizationaudit.LogAuthorizationAuditPageVO;
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
