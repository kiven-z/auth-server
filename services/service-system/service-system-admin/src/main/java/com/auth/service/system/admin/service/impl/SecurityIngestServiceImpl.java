package com.auth.service.system.admin.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.auth.module.security.contract.event.OperationLogPayloadEvent;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.service.system.admin.convert.LogOperationPayloadConverter;
import com.auth.service.system.admin.mapper.LogAuthorizationAuditMapper;
import com.auth.service.system.admin.mapper.LogOperationMapper;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import com.auth.service.system.admin.service.SecurityIngestService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 安全类内部上报数据落库实现
 *
 * @author Bunny
 */
@Service
public class SecurityIngestServiceImpl implements SecurityIngestService {

	/**
	 * 方法参数 JSON 最大长度
	 */
	private static final int MAX_PARAM_JSON_LENGTH = 2048;

	/**
	 * 异常信息最大长度
	 */
	private static final int MAX_EXCEPTION_LENGTH = 2048;

	private final LogAuthorizationAuditMapper logAuthorizationAuditMapper;

	private final LogOperationMapper logOperationMapper;

	public SecurityIngestServiceImpl(LogAuthorizationAuditMapper logAuthorizationAuditMapper,
			LogOperationMapper logOperationMapper) {
		this.logAuthorizationAuditMapper = logAuthorizationAuditMapper;
		this.logOperationMapper = logOperationMapper;
	}

	private static String truncate(String value, int maxLen) {
		if (CharSequenceUtil.isEmpty(value)) {
			return value;
		}
		return value.length() <= maxLen ? value : value.substring(0, maxLen);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void append(SecurityAuthorizationAuditPayloadEvent event) {
		LogAuthorizationAuditEntity entity = new LogAuthorizationAuditEntity();
		entity
			.setEventType(event.getEventType() != null ? event.getEventType().name() : SecurityEventType.DENIED.name());
		entity.setRequiredPermission(event.getRequiredAuthority());
		entity.setRequestMethod(event.getRequestMethod());
		entity.setRequestUri(event.getRequestUri());
		entity.setRequestIp(event.getRequestIp());
		entity.setDecisionReason(event.getDecisionReason());
		entity.setExceptionMessage(truncate(event.getExceptionMessage(), MAX_EXCEPTION_LENGTH));
		entity.setClassName(event.getClassName());
		entity.setMethodName(event.getMethodName());

		Map<String, Object> params = event.getMethodParams();
		if (MapUtil.isNotEmpty(params)) {
			String json = JSON.toJSONString(params);
			String truncate = truncate(json, MAX_PARAM_JSON_LENGTH);
			entity.setMethodParams(truncate);
		}

		Long principalId = event.getUserId();
		entity.setCreateUser(principalId != null ? principalId : 0L);

		logAuthorizationAuditMapper.insert(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void append(OperationLogPayloadEvent payload) {
		Long principalUserId = payload.getUserId();
		LogOperationEntity entity = LogOperationPayloadConverter.INSTANCE.toInsertEntity(payload, principalUserId);
		logOperationMapper.insert(entity);
	}

}
