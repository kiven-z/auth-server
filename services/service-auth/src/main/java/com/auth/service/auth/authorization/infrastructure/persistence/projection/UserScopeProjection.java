package com.auth.service.auth.authorization.infrastructure.persistence.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * user_scope 只读查询投影
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserScopeProjection implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "范围类型：ALL/SELF/DEPT/DEPT_AND_CHILD")
	private String scopeType;

	@Schema(description = "scope_dept_ids JSON 字符串")
	private String scopeDeptIds;

}
