package com.auth.service.system.common.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.module.security.contract.spi.AuthorizationAuditHandler;
import com.auth.service.system.log.mapper.LogAuthorizationAuditMapper;
import com.auth.service.system.log.model.entity.LogAuthorizationAuditEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 认证审计处理器：将授权决策事件持久化至 {@code log_authorization_audit}。
 *
 * @author Bunny
 */
@Slf4j
@Component
public class AuthAuditHandler implements AuthorizationAuditHandler {

	private static final int MAX_PARAM_JSON_LENGTH = 2048;

	private static final int MAX_EXCEPTION_LENGTH = 2048;

	private final LogAuthorizationAuditMapper logAuthorizationAuditMapper;

	public AuthAuditHandler(LogAuthorizationAuditMapper logAuthorizationAuditMapper) {
		this.logAuthorizationAuditMapper = logAuthorizationAuditMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(SecurityAuthorizationAuditPayloadEvent event) {
		try {
			LogAuthorizationAuditEntity entity = toEntity(event);
			logAuthorizationAuditMapper.insert(entity);
		}
		catch (RuntimeException ex) {
			log.error("Failed to persist authorization audit event", ex);
		}
	}

	/**
	 * 将契约事件转为持久化实体
	 * @param event 事件
	 * @return 实体
	 */
	private static LogAuthorizationAuditEntity toEntity(SecurityAuthorizationAuditPayloadEvent event) {
		LogAuthorizationAuditEntity e = new LogAuthorizationAuditEntity();
		e.setEventType(event.getEventType() != null ? event.getEventType().name() : SecurityEventType.DENIED.name());
		e.setRequiredPermission(event.getRequiredAuthority());
		e.setRequestMethod(event.getRequestMethod());
		e.setRequestUri(event.getRequestUri());
		e.setRequestIp(event.getRequestIp());
		e.setDecisionReason(event.getDecisionReason());
		e.setExceptionMessage(truncate(event.getExceptionMessage(), MAX_EXCEPTION_LENGTH));
		e.setClassName(event.getClassName());
		e.setMethodName(event.getMethodName());
		e.setMethodParams(methodParamsJson(event.getMethodParams()));
		Long principalId = event.getUserId();
		e.setCreateUser(principalId != null ? principalId : 0L);
		return e;
	}

	private static String methodParamsJson(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return null;
		}
		String json = JSON.toJSONString(params);
		return truncate(json, MAX_PARAM_JSON_LENGTH);
	}

	private static String truncate(String value, int maxLen) {
		if (CharSequenceUtil.isEmpty(value)) {
			return value;
		}
		return value.length() <= maxLen ? value : value.substring(0, maxLen);
	}

}
