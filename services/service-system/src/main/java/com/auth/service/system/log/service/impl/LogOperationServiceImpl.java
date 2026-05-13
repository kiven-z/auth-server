package com.auth.service.system.log.service.impl;

import com.auth.service.system.log.mapper.LogOperationMapper;
import com.auth.service.system.log.model.entity.LogOperationEntity;
import com.auth.service.system.log.service.LogOperationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * {@link LogOperationService} implementation backed by {@link LogOperationMapper}.
 *
 * @author Bunny
 */
@Service
public class LogOperationServiceImpl extends ServiceImpl<LogOperationMapper, LogOperationEntity>
		implements LogOperationService {

}
