package com.auth.service.system.admin.support.user;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.mapper.SysUserMapper;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户写操作前的存在性校验
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserReferenceChecker {

	private final SysUserMapper sysUserMapper;

	/**
	 * 加载用户，不存在则抛业务异常
	 * @param userId 用户 ID
	 * @return 用户实体
	 */
	public UserEntity getExistingActive(Long userId) {
		UserEntity existing = sysUserMapper.selectById(userId);
		if (existing == null) {
			throw new SystemBusinessException(SystemCommonResultCode.USER_NOT_FOUND, userId);
		}
		return existing;
	}

}
