package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.mapper.log.LogUserPasswordHistoryMapper;
import com.auth.service.system.admin.model.entity.LogUserPasswordHistoryEntity;
import com.auth.service.system.admin.service.admin.LogUserPasswordHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 密码历史日志写服务实现。
 *
 * @author Bunny
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LogUserPasswordHistoryServiceImpl
		extends ServiceImpl<LogUserPasswordHistoryMapper, LogUserPasswordHistoryEntity>
		implements LogUserPasswordHistoryService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordChange(Long userId, String passwordHash, String changeIp) {
		LogUserPasswordHistoryEntity entity = new LogUserPasswordHistoryEntity();
		entity.setUserId(userId);
		entity.setPasswordHash(passwordHash);
		entity.setChangeIp(changeIp);
		save(entity);
	}

}
