package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.mapper.log.LogOperationMapper;
import com.auth.service.system.admin.model.entity.LogOperationEntity;
import com.auth.service.system.admin.service.admin.LogOperationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 操作日志写服务实现。
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LogOperationServiceImpl extends ServiceImpl<LogOperationMapper, LogOperationEntity>
		implements LogOperationService {

}
