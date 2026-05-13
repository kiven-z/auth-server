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
	 * {@inheritDoc}
	 */
	@Override
	public void append(OperationLogPayload payload) {
		Long uid = payload.getUserId() != null ? payload.getUserId() : 0L;
		LogOperationEntity entity = new LogOperationEntity();
		entity.setUserId(uid);
		entity.setOperationType(payload.getOperationType());
		entity.setModule(payload.getModule());
		entity.setTargetType(CharSequenceUtil.blankToDefault(payload.getTargetType(), null));
		entity.setTargetId(CharSequenceUtil.blankToDefault(payload.getTargetId(), null));
		entity.setRequestMethod(payload.getRequestMethod());
		entity.setRequestUri(payload.getRequestUri());
		entity.setRequestParams(CharSequenceUtil.blankToDefault(payload.getRequestParams(), null));
		entity.setRequestBody(CharSequenceUtil.blankToDefault(payload.getRequestBody(), null));
		entity.setResponseStatus(payload.getResponseStatus());
		entity.setResponseMessage(CharSequenceUtil.blankToDefault(payload.getResponseMessage(), null));
		entity.setExecutionTimeMs(payload.getExecutionTimeMs());
		entity.setOldValue(CharSequenceUtil.blankToDefault(payload.getOldValueJson(), null));
		entity.setNewValue(CharSequenceUtil.blankToDefault(payload.getNewValueJson(), null));
		entity.setIpAddress(payload.getIpAddress());
		entity.setUserAgent(CharSequenceUtil.blankToDefault(payload.getUserAgent(), null));
		entity.setCreateUser(uid);
		entity.setUpdateUser(uid);
		entity.setIsDeleted(Boolean.FALSE);

		logOperationMapper.insert(entity);
	}

}
