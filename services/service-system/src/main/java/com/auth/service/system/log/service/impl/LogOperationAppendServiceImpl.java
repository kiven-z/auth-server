package com.auth.service.system.log.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.event.OperationLogPayload;
import com.auth.service.system.log.mapper.LogOperationMapper;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.auth.service.system.log.service.LogOperationAppendService;
import org.springframework.stereotype.Service;

/**
 * 操作日志追加写入实现。
 *
 * @author Bunny
 */
@Service
public class LogOperationAppendServiceImpl implements LogOperationAppendService {

	private final LogOperationMapper logOperationMapper;

	public LogOperationAppendServiceImpl(LogOperationMapper logOperationMapper) {
		this.logOperationMapper = logOperationMapper;
	}

	/**
	 * 将负载映射为实体（异步线程无安全上下文，审计字段在此显式赋值）。
	 * @param payload 负载
	 * @return 实体
	 */
	private static LogOperationEntity toEntity(OperationLogPayload payload) {
		Long uid = payload.getUserId() != null ? payload.getUserId() : 0L;
		LogOperationEntity e = new LogOperationEntity();
		e.setUserId(uid);
		e.setOperationType(payload.getOperationType());
		e.setModule(payload.getModule());
		e.setTargetType(blankToNull(payload.getTargetType()));
		e.setTargetId(blankToNull(payload.getTargetId()));
		e.setRequestMethod(payload.getRequestMethod());
		e.setRequestUri(payload.getRequestUri());
		e.setRequestParams(blankToNull(payload.getRequestParams()));
		e.setRequestBody(blankToNull(payload.getRequestBody()));
		e.setResponseStatus(payload.getResponseStatus());
		e.setResponseMessage(blankToNull(payload.getResponseMessage()));
		e.setExecutionTimeMs(payload.getExecutionTimeMs());
		e.setOldValue(blankToNull(payload.getOldValueJson()));
		e.setNewValue(blankToNull(payload.getNewValueJson()));
		e.setIpAddress(payload.getIpAddress());
		e.setUserAgent(blankToNull(payload.getUserAgent()));
		e.setCreateUser(uid);
		e.setUpdateUser(uid);
		e.setIsDeleted(Boolean.FALSE);
		return e;
	}

	private static String blankToNull(String s) {
		return CharSequenceUtil.isBlank(s) ? null : s;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void append(OperationLogPayload payload) {
		LogOperationEntity entity = toEntity(payload);
		logOperationMapper.insert(entity);
	}

}
