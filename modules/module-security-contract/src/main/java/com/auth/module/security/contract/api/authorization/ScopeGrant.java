package com.auth.module.security.contract.api.authorization;

import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 运行时数据范围
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
	private final ScopeGrantType scopeType;

	/**
	 * 范围取值列表（JSON 数组解析结果），如部门 ID / 岗位 ID 等
	 */
	@Builder.Default
	private final List<String> values = Collections.emptyList();

}
