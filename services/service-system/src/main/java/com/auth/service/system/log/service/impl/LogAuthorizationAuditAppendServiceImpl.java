package com.auth.service.system.log.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import com.auth.module.security.contract.event.SecurityEventType;
import com.auth.service.system.log.mapper.LogAuthorizationAuditMapper;
import com.auth.service.system.log.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.log.service.LogAuthorizationAuditAppendService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 授权审计追加写入实现。
 *
 * @author Bunny
 */
@Service
public class LogAuthorizationAuditAppendServiceImpl implements LogAuthorizationAuditAppendService {

	/**
	 * 方法参数 JSON 最大长度
	 */
	private static final int MAX_PARAM_JSON_LENGTH = 2048;

	/**
	 * 异常信息最大长度
	 */
	private static final int MAX_EXCEPTION_LENGTH = 2048;

	private final LogAuthorizationAuditMapper logAuthorizationAuditMapper;

	public LogAuthorizationAuditAppendServiceImpl(LogAuthorizationAuditMapper logAuthorizationAuditMapper) {
		this.logAuthorizationAuditMapper = logAuthorizationAuditMapper;
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

		// 记录方法传参
		Map<String, Object> params = event.getMethodParams();
		if (MapUtil.isNotEmpty(params)) {
			String json = JSON.toJSONString(params);
			String truncate = truncate(json, MAX_PARAM_JSON_LENGTH);
			e.setMethodParams(truncate);
		}

		Long principalId = event.getUserId();
		e.setCreateUser(principalId != null ? principalId : 0L);
		return e;
	}

	/**
	 * 截取字符串
	 * @param value 字符串
	 * @param maxLen 最大长度
	 * @return 截取后的字符串
	 */
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
		LogAuthorizationAuditEntity entity = toEntity(event);
		logAuthorizationAuditMapper.insert(entity);
	}

}
