package com.auth.module.file.api.constants;

import lombok.experimental.UtilityClass;

/**
 * 文件删除来源常量
 *
 * @author Bunny
 */
@UtilityClass
public class FileDeleteSourceConstants {

	/**
	 * 用户自行删除（进入个人回收站）
	 */
	public static final String USER_SELF = "USER_SELF";

	/**
	 * 管理员删除（不进入用户回收站）
	 */
	public static final String ADMIN_ACTION = "ADMIN_ACTION";

}
