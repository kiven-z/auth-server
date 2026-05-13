package com.auth.service.system.log.service.impl;

import com.auth.module.security.contract.event.OperationLogPayload;
import com.auth.service.system.log.convert.LogOperationPayloadConvert;
import com.auth.service.system.log.mapper.LogOperationMapper;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.auth.service.system.log.service.LogOperationAppendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 操作日志追加写入实现。
 *
 * @author Bunny
 */
@Service
public class LogOperationAppendServiceImpl extends ServiceImpl<LogOperationMapper, LogOperationEntity>
		implements LogOperationAppendService {

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
		LogOperationEntity entity = LogOperationPayloadConvert.INSTANCE.toInsertEntity(payload, uid);
		logOperationMapper.insert(entity);
	}

}
