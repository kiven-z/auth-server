package com.auth.service.system.log.service;

import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * {@code log_operation} application service: append from audit pipeline and remote
 * ingest; list/detail/delete APIs can be added here alongside existing query controllers.
 *
 * @author Bunny
 */
public interface LogOperationService extends IService<LogOperationEntity> {

}
