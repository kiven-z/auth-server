package com.auth.service.auth.authorization.infrastructure.persistence.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户失效状态查询投影
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserInvalidationStateProjection {

	@Schema(description = "用户ID")
	private Long userId;

	@Schema(description = "用户状态（0=禁用,1=正常,2=锁定）")
	private Integer status;

	@Schema(description = "逻辑删除标记（0=否,1=是）")
	private Integer isDeleted;

}
