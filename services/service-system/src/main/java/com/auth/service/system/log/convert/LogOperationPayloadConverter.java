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
	 * 将负载映射为待插入实体
	 * @param payload 操作日志负载
	 * @param principalUserId 操作用户 ID；匿名或未认证时为null
	 * @return 持久化实体
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "remark", ignore = true)
	@Mapping(target = "userId", source = "principalUserId")
	@Mapping(target = "createUser", source = "principalUserId")
	@Mapping(target = "updateUser", source = "principalUserId")
	@Mapping(target = "targetType", source = "payload.targetType", qualifiedByName = "blankToNull")
	@Mapping(target = "requestParams", source = "payload.requestParams", qualifiedByName = "blankToNull")
	@Mapping(target = "responseMessage", source = "payload.responseMessage", qualifiedByName = "blankToNull")
	@Mapping(target = "userAgent", source = "payload.userAgent", qualifiedByName = "blankToNull")
	@BeanMapping(ignoreUnmappedSourceProperties = { "userId", "className", "methodName", "timestamp" })
	LogOperationEntity toInsertEntity(OperationLogPayloadEvent payload, Long principalUserId);

}
