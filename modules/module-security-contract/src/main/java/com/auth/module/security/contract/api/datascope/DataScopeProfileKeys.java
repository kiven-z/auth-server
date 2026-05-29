package com.auth.module.security.contract.api.datascope;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

/**
 * 数据权限在 {@code AuthProfile.dataScopes} 中的键（v1 仅部门维）
 *
 * @author Bunny
 */
@UtilityClass
public class DataScopeProfileKeys {

	/**
	 * 部门维默认画像键
	 */
	public static final String DEFAULT = "DEPT::default";

	/**
	 * 按资源键生成画像 Map 键
	 * @param resourceCode 资源键
	 * @return 画像键，如 {@code DEPT::order}
	 */
	public static String forResource(String resourceCode) {
		if (CharSequenceUtil.isBlank(resourceCode)) {
			return DEFAULT;
		}
		return "DEPT::" + resourceCode;
	}

}
