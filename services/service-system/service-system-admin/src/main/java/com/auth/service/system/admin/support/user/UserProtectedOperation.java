package com.auth.service.system.admin.support.user;

import com.auth.common.data.model.enums.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户受保护写操作类型
 *
 * @author Bunny
 */
@Getter
@RequiredArgsConstructor
public enum UserProtectedOperation {

	/**
	 * 删除
	 */
	DELETE(true),

	/**
	 * 设为正常
	 */
	STATUS_NORMAL(false),

	/**
	 * 设为禁用
	 */
	STATUS_DISABLED(true),

	/**
	 * 设为锁定
	 */
	STATUS_LOCKED(true);

	/**
	 * 是否禁止操作当前登录账号
	 */
	private final boolean restrictSelf;

	/**
	 * 根据目标状态解析写操作类型
	 * @param status 目标状态码
	 * @return 写操作类型
	 */
	public static UserProtectedOperation fromStatus(int status) {
		UserStatus userStatus = UserStatus.of(status);
		if (userStatus == null) {
			throw new IllegalArgumentException("Unsupported user status: " + status);
		}
		return switch (userStatus) {
			case NORMAL -> STATUS_NORMAL;
			case DISABLED -> STATUS_DISABLED;
			case LOCKED -> STATUS_LOCKED;
		};
	}

}
