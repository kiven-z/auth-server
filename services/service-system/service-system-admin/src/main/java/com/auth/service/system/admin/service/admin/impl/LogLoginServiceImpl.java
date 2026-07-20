package com.auth.service.system.admin.service.admin.impl;

import com.auth.module.platform.persistence.model.LoginLogEntity;
import com.auth.service.system.admin.mapper.log.LogLoginLogMapper;
import com.auth.service.system.admin.service.admin.LogLoginService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录日志写服务实现。
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LogLoginServiceImpl extends ServiceImpl<LogLoginLogMapper, LoginLogEntity> implements LogLoginService {

}
