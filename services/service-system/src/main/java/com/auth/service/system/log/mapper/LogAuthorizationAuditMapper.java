package com.auth.service.system.log.mapper;

import com.auth.service.system.log.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.log.model.po.LogAuthorizationAuditDetailRowPO;
import com.auth.service.system.log.model.po.LogAuthorizationAuditPageRowPO;
import com.auth.service.system.log.model.query.LogAuthorizationAuditQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 权限决策审计日志 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface LogAuthorizationAuditMapper extends BaseMapper<LogAuthorizationAuditEntity> {

	/**
	 * 分页查询权限决策审计
	 * @param page 分页
	 * @param query 条件
	 * @return 分页数据
	 */
	IPage<LogAuthorizationAuditPageRowPO> selectListByPage(@Param("page") Page<LogAuthorizationAuditEntity> page,
			@Param("query") LogAuthorizationAuditQuery query);

	/**
	 * 详情
	 * @param id 主键
	 * @return 详情投影
	 */
	LogAuthorizationAuditDetailRowPO selectDetailById(@Param("id") Long id);

}
