package com.auth.common.mapstruct.qualifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import cn.hutool.core.util.BooleanUtil;
import lombok.experimental.UtilityClass;
import org.mapstruct.Named;

/**
 * Excel 导出列映射辅助方法
 *
 * @author Bunny
 */
@UtilityClass
public class MapStructExportQualifiers {

	/**
	 * 导出时间列默认格式
	 */
	public static final DateTimeFormatter EXPORT_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * 启用状态 → 导出文案
	 * @param status 布尔状态
	 * @return 中文标签
	 */
	@Named("statusLabel")
	public String statusLabel(Boolean status) {
		return BooleanUtil.isTrue(status) ? "启用" : "禁用";
	}

	/**
	 * 时间 → 导出文本
	 * @param dateTime 时间
	 * @return 格式化文本，空则返回空串
	 */
	@Named("dateTimeText")
	public String dateTimeText(LocalDateTime dateTime) {
		return dateTime == null ? "" : dateTime.format(EXPORT_DATE_TIME);
	}

}
