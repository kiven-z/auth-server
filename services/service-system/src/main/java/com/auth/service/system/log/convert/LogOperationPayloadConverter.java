package com.auth.service.system.log.convert;

import com.auth.common.mapstruct.qualifier.MapStructStringQualifiers;
import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * {@link OperationLogPayloadEvent} → {@link LogOperationEntity} 映射（契约层入站持久化）。
 *
 * @author Bunny
 */
@Mapper(uses = MapStructStringQualifiers.class, unmappedTargetPolicy = ReportingPolicy.ERROR,
		unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface LogOperationPayloadConverter {

	LogOperationPayloadConverter INSTANCE = Mappers.getMapper(LogOperationPayloadConverter.class);

	/**
	 * 将负载映射为待插入实体；审计用户与操作用户均使用 {@code effectiveUserId}（通常为已规范化的用户主键）。
	 * @param payload 操作日志负载
	 * @param effectiveUserId 操作用户 ID（未登录场景调用方应传入 0）
	 * @return 持久化实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "remark", ignore = true)
	@Mapping(target = "userId", source = "effectiveUserId")
	@Mapping(target = "createUser", source = "effectiveUserId")
	@Mapping(target = "updateUser", source = "effectiveUserId")
	@Mapping(target = "isDeleted", constant = "false")
	@Mapping(target = "targetType", source = "payload.targetType", qualifiedByName = "blankToNull")
	@Mapping(target = "requestParams", source = "payload.requestParams", qualifiedByName = "blankToNull")
	@Mapping(target = "responseMessage", source = "payload.responseMessage", qualifiedByName = "blankToNull")
	@Mapping(target = "userAgent", source = "payload.userAgent", qualifiedByName = "blankToNull")
	@BeanMapping(ignoreUnmappedSourceProperties = { "userId", "className", "methodName", "timestamp" })
	LogOperationEntity toInsertEntity(OperationLogPayloadEvent payload, Long effectiveUserId);

}
