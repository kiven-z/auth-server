package com.auth.service.system.admin.convert.ingest;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * 操作日志事件到持久化实体的映射。
 *
 * @author Bunny
 */
@UtilityClass
public class SecurityIngestEntityAssembler {

	/**
	 * 异常信息最大长度
	 */
	private static final int MAX_EXCEPTION_LENGTH = 2048;

	/**
	 * 方法参数 JSON 最大长度
	 */
	private static final int MAX_PARAM_JSON_LENGTH = 2048;

	/**
	 * 将操作日志负载映射为待插入实体。
	 * @param payload 操作日志负载
	 * @param principalUserId 操作用户 ID；匿名或未认证时为 null
	 * @return 持久化实体
	 */
	public static LogOperationEntity toEntity(OperationLogPayloadEvent payload, Long principalUserId) {
		LogOperationEntity entity = new LogOperationEntity();
		entity.setUserId(principalUserId);
		entity.setCreateUser(principalUserId);
		entity.setUpdateUser(principalUserId);
		entity.setOperationType(payload.getOperationType());
		entity.setModule(payload.getModule());
		entity.setTargetType((payload.getTargetType()));
		entity.setTargetId(payload.getTargetId());
		entity.setRequestMethod(payload.getRequestMethod());
		entity.setRequestUri(payload.getRequestUri());
		entity.setRequestParams((payload.getRequestParams()));
		entity.setResponseStatus(payload.getResponseStatus());
		entity.setResponseMessage((payload.getResponseMessage()));
		entity.setExecutionTimeMs(payload.getExecutionTimeMs());
		entity.setIpAddress(payload.getIpAddress());
		entity.setUserAgent((payload.getUserAgent()));
		return entity;
	}

	/**
	 * 将授权审计负载映射为待插入实体。
	 * @param event 授权审计负载
	 * @return 持久化实体
	 */
	public static LogAuthorizationAuditEntity toEntity(SecurityAuthorizationAuditPayloadEvent event) {
		LogAuthorizationAuditEntity entity = new LogAuthorizationAuditEntity();
		entity.setEventType(ObjectUtil.defaultIfNull(event.getEventType(), SecurityEventType.DENIED).name());
		entity.setRequiredPermission(event.getRequiredAuthority());
		entity.setRequestMethod(event.getRequestMethod());
		entity.setRequestUri(event.getRequestUri());
		entity.setRequestIp(event.getRequestIp());
		entity.setDecisionReason(event.getDecisionReason());
		entity.setExceptionMessage(CharSequenceUtil.subPre(event.getExceptionMessage(), MAX_EXCEPTION_LENGTH));
		entity.setClassName(event.getClassName());
		entity.setMethodName(event.getMethodName());

		Map<String, Object> params = event.getMethodParams();
		if (MapUtil.isNotEmpty(params)) {
			String json = JSON.toJSONString(params);
			entity.setMethodParams(CharSequenceUtil.subPre(json, MAX_PARAM_JSON_LENGTH));
		}

		entity.setCreateUser(ObjectUtil.defaultIfNull(event.getUserId(), 0L));
		return entity;
	}

}
