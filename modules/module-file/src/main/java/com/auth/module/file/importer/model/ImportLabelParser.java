package com.auth.module.file.importer.model;

import lombok.experimental.UtilityClass;

/**
 * 导入文案 → 领域值转换工具
 *
 * @author Bunny
 */
@UtilityClass
public class ImportLabelParser {

	/**
	 * 状态文案 → 布尔值
	 * @param label Excel 中填写的状态文案
	 * @return 布尔启用状态
	 */
	public static Boolean parseStatus(String label) {
		return !"停用".equals(label);
	}

}
