package com.auth.module.file.export.definition;

import cn.hutool.core.bean.BeanUtil;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Objects;

/**
 * 导出行 → Map（fieldKey 对齐 SpreadsheetRow 字段名）
 *
 * @author Bunny
 */
@UtilityClass
public class ExportRowMaps {

	/**
	 * 将表格行 Bean 转为导出 Map（忽略 null，键为字段名）
	 * @param spreadsheetRow 含导出字段的行对象
	 * @return Map；入参为空时返回空 Map
	 */
	public static Map<String, Object> fromBean(Object spreadsheetRow) {
		if (spreadsheetRow == null) {
			return Map.of();
		}
		return Objects.requireNonNullElse(BeanUtil.beanToMap(spreadsheetRow, false, true), Map.of());
	}

}
