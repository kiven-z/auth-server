package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.mapper.log.LogAuthorizationAuditMapper;
import com.auth.service.system.admin.model.entity.LogAuthorizationAuditEntity;
import com.auth.service.system.admin.service.admin.LogAuthorizationAuditService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限决策审计日志写服务实现。
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LogAuthorizationAuditServiceImpl extends
		ServiceImpl<LogAuthorizationAuditMapper, LogAuthorizationAuditEntity> implements LogAuthorizationAuditService {

}
