package com.auth.module.security.contract.api.authorization;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

/**
 * 部门维数据范围
 *
 * @author Bunny
 */
@JsonDeserialize(builder = ScopeGrant.ScopeGrantBuilder.class)
@Getter
@Builder
public class ScopeGrant implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 授权范围类型
	 */
	private final DataScopeStorageType scopeType;

	/**
	 * 范围取值列表
	 */
	@Builder.Default
	private final List<Long> values = Collections.emptyList();

}
