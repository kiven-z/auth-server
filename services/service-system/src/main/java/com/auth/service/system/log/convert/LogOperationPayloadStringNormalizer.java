package com.auth.service.system.log.convert;

import cn.hutool.core.text.CharSequenceUtil;
import org.mapstruct.Named;

/**
 * 操作日志负载映射辅助：将空白字符序列归一为 {@code null}，供 MapStruct {@code qualifiedByName} 引用。
 *
 * @author Bunny
 */
public class LogOperationPayloadStringNormalizer {

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
