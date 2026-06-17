package com.auth.service.auth.infrastructure.persistence.mapper;

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

	private String eventId;

	private String changeKind;

	private Boolean processing;

	private LocalDateTime processedAtStart;

	private LocalDateTime processedAtEnd;

}
