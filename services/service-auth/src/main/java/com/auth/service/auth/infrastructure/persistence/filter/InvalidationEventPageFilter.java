package com.auth.service.auth.infrastructure.persistence.filter;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 授权失效幂等事件分页查询过滤参数（Mapper 内部使用，与应用层 Criteria 解耦）
 *
 * @author Bunny
 */
@Getter
@Setter
public class InvalidationEventPageFilter {

	/**
	 * 业务事件 ID（前缀匹配）
	 */
	private String eventId;

	/**
	 * 变更维度（AuthorizationChangeKind）
	 */
	private String changeKind;

	/**
	 * 是否处理中占位（impacted_user_count = -1）
	 */
	private Boolean processing;

	/**
	 * 处理完成时间（开始）
	 */
	private LocalDateTime processedAtStart;

	/**
	 * 处理完成时间（结束）
	 */
	private LocalDateTime processedAtEnd;

}
