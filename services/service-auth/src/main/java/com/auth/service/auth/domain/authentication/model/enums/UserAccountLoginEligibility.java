package com.auth.service.auth.domain.authentication.model.enums;

/**
 * 用户账户是否允许进入登录凭证校验（仅看账户状态位，不含登录失败锁定计数）。
 *
 * @author Bunny
 */
public enum UserAccountLoginEligibility {

	/**
	 * 允许继续登录流程
	 */
	ALLOWED,

	/**
	 * 账户已禁用
	 */
	DISABLED,

	/**
	 * 账户状态为锁定（与平台用户 status 字段约定一致）
	 */
	STATUS_LOCKED;

	/**
	 * 与 {@link com.auth.module.platform.persistence.model.UserEntity#getStatus()} 对齐：0=禁用
	 */
	private static final int SYS_USER_STATUS_DISABLED = 0;

	/**
	 * 与 {@link com.auth.module.platform.persistence.model.UserEntity#getStatus()} 对齐：2=锁定
	 */
	private static final int SYS_USER_STATUS_LOCKED = 2;

	/**
	 * 根据原始状态值判定是否允许登录。
	 * @param rawStatus 用户状态，可能为 null
	 * @return 登录资格枚举
	 */
	public static UserAccountLoginEligibility fromStatus(Integer rawStatus) {
		if (rawStatus == null || rawStatus == SYS_USER_STATUS_DISABLED) {
			return DISABLED;
		}
		if (rawStatus == SYS_USER_STATUS_LOCKED) {
			return STATUS_LOCKED;
		}
		return ALLOWED;
	}

}
