package com.auth.service.system.admin.support.user;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 用户写操作前的受保护账号校验
 *
 * @author Bunny
 */
@Component
public class UserOperationChecker {

	/**
	 * 校验目标用户是否允许当前写操作
	 * @param userIds 待操作用户主键
	 */
	public void requireOperable(Collection<Long> userIds) {
		Long currentUserId = SecurityUserUtils.getUserId();

		for (Long userId : userIds) {
			boolean protectedTarget = PermissionConstant.isSuperAdmin(userId)
					|| (currentUserId != null && currentUserId.equals(userId));

			if (protectedTarget) {
				throw new SystemBusinessException(SystemAdminResultCode.USER_OPERATION_FORBIDDEN, userId);
			}
		}
	}

}