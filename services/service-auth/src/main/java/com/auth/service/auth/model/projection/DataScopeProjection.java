package com.auth.service.auth.model.projection;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * data_scope_grant_table 查询投影（与 Mapper 列别名一致）。
 *
 * @author Bunny
 */
@Getter
@Setter
public class DataScopeProjection implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "授权主体类型：USER / ROLE...")
	private String subjectType;

	@Schema(description = "数据范围资源键（与注解中的 resourceCode 对齐）")
	private String scopeResourceCode;

	@Schema(description = "数据维度类型（如 DEPT / POST..）")
	private String dimensionType;

	@Schema(description = "范围类型（库内存量字符串）")
	private String scopeType;

	@Schema(description = "scope_subject_ids JSON 序列化字符串")
	private String scopeSubjectIds;

}
