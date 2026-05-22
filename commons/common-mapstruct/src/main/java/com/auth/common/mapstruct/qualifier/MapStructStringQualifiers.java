package com.auth.common.mapstruct.qualifier;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;
import org.mapstruct.Named;

/**
 * 多层辅助类可以组合使用
 *
 * @author Bunny
 */
@UtilityClass
public class MapStructStringQualifiers {

	/**
	 * 空白（含 null、空串、仅空白）时返回 {@code null}，否则返回原字符串。
	 * @param value 原始值
	 * @return 归一后的值
	 */
	@Named("blankToNull")
	public String blankToNull(String value) {
		return CharSequenceUtil.blankToDefault(value, null);
	}

	/**
	 * JDBC 整型状态 → API 布尔（1=成功 → true，0=失败 → false）
	 * @param dbStatus 库表 0/1
	 * @return API 语义
	 */
	@Named("dbIntToSuccessBoolean")
	public Boolean dbIntToSuccessBoolean(Integer dbStatus) {
		return dbStatus != null && dbStatus == 1;
	}

}
