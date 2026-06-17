package com.auth.service.auth.infrastructure.persistence.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * role_scope 只读查询投影
 *
 * @author Bunny
 */
@Getter
@Setter
public class RoleScopeProjection implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "角色编码")
	private String roleCode;

	@Schema(description = "范围类型：ALL/SELF/DEPT/DEPT_AND_CHILD")
	private String scopeType;

	@Schema(description = "scope_dept_ids JSON 字符串")
	private String scopeDeptIds;

}
