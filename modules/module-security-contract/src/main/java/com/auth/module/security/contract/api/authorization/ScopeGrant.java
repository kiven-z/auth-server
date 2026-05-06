package com.auth.module.security.contract.api.authorization;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.auth.module.security.contract.api.granttable.ScopeGrantType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Builder;
import lombok.Getter;

/**
 * 单个授权主体的数据范围（与 data_scope_grant_table 的 scope_type、scope_subject_ids 对应）。
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
