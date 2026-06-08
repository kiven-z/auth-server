package com.auth.service.system.admin.support.user;

import lombok.experimental.UtilityClass;

/**
 * 用户管理默认值
 *
 * @author Bunny
 */
@UtilityClass
public class SysUserDefaults {

	/**
	 * 新增用户初始明文密码（入库前 BCrypt 加密）
	 */
	public static final String DEFAULT_PASSWORD = "admin123";

}
