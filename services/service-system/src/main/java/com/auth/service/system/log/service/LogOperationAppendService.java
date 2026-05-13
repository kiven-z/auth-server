package com.auth.service.system.log.service;

import com.auth.module.security.contract.event.OperationLogPayload;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 将操作日志负载追加写入
 *
 * @author Bunny
 */
public interface LogOperationAppendService extends IService<LogOperationEntity> {

	/**
	 * 追加一条操作日志
	 * @param payload 契约负载
	 */
	void append(OperationLogPayload payload);

}
