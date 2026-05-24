package com.auth.service.auth.authorization.infrastructure.persistence;

import com.auth.service.auth.authorization.infrastructure.persistence.projection.AuthorizationInvalidationEventProjection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 授权失效幂等事件 Mapper。
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
	 * 插入已处理事件记录（{@code event_id} 唯一约束保证幂等）。
	 * @param record 事件投影
	 * @return 插入行数
	 */
	int insertProcessedEvent(AuthorizationInvalidationEventProjection record);

}
