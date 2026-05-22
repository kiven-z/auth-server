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

}
