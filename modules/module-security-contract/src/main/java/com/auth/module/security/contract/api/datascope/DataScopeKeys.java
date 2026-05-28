package com.auth.module.security.contract.api.datascope;

import com.auth.module.security.contract.api.granttable.DataScopeKeyUtil;
import lombok.experimental.UtilityClass;

/**
 * AuthProfile.dataScopes 标准键
 *
 * @author Bunny
 */
@UtilityClass
public class DataScopeKeys {

	/**
	 * 部门维默认资源键（DEPT::default）
	 */
	public static final String DEPT_DEFAULT = DataScopeKeyUtil.compose(DataScopeKeyUtil.DEFAULT_DIMENSION,
			DataScopeKeyUtil.DEFAULT_RESOURCE_CODE);

}
