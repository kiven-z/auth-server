package com.auth.service.system.admin.model.constants;

import lombok.experimental.UtilityClass;

/**
 * admin 模块批量持久化批次大小。
 *
 * @author Bunny
 */
@UtilityClass
public class AdminBatchSizes {

	/**
	 * Excel 导入与批量新增时的批次大小
	 */
	public static final int IMPORT_SAVE = 500;

}
