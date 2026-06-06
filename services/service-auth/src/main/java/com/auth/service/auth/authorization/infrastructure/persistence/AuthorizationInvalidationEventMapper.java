package com.auth.service.auth.authorization.infrastructure.persistence;

import com.auth.service.auth.authorization.admin.model.po.AuthorizationInvalidationEventDetailRowPO;
import com.auth.service.auth.authorization.admin.model.po.AuthorizationInvalidationEventPageRowPO;
import com.auth.service.auth.authorization.admin.model.query.AuthorizationInvalidationEventQuery;
import com.auth.service.auth.authorization.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 授权失效幂等事件 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface AuthorizationInvalidationEventMapper {

	/**
	 * 按事件 ID 查询已处理记录。
	 * @param eventId 业务事件 ID
	 * @return 投影，不存在时为 null
	 */
	AuthorizationInvalidationEventProjection selectByEventId(@Param("eventId") String eventId);

	/**
	 * 插入处理中占位记录
	 * @param projection 事件投影（计数为
	 * @return 插入行数
	 */
	int insertProcessingClaim(AuthorizationInvalidationEventProjection projection);

	/**
	 * 将占位行更新为最终处理结果。
	 * @param projection 事件投影
	 * @return 更新行数
	 */
	int updateProcessedOutcome(AuthorizationInvalidationEventProjection projection);

	/**
	 * 删除处理中占位行（失败回滚时释放 eventId）。
	 * @param eventId 业务事件 ID
	 * @return 删除行数
	 */
	int deleteProcessingClaim(@Param("eventId") String eventId);

	/**
	 * 分页查询幂等事件（运维只读）
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	IPage<AuthorizationInvalidationEventPageRowPO> selectListByPage(
			@Param("page") Page<AuthorizationInvalidationEventPageRowPO> page,
			@Param("query") AuthorizationInvalidationEventQuery query);

	/**
	 * 按主键查询详情
	 * @param id 主键
	 * @return 详情投影
	 */
	AuthorizationInvalidationEventDetailRowPO selectDetailById(@Param("id") Long id);

}
