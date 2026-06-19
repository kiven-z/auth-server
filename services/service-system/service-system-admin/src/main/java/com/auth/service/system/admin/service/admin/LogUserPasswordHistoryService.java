package com.auth.service.system.admin.service.admin;

import com.auth.service.system.admin.model.entity.LogUserPasswordHistoryEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 密码历史日志写服务。
 *
 * @author Bunny
 */
public interface LogUserPasswordHistoryService extends IService<LogUserPasswordHistoryEntity> {

	/**
	 * 记录一次密码变更
	 * @param userId 用户主键
	 * @param passwordHash 加密后的密码
	 * @param changeIp 客户端 IP
	 */
	void recordChange(Long userId, String passwordHash, String changeIp);

}
