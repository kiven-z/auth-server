package com.auth.service.auth.authorization.infrastructure.persistence.projection;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户失效状态查询投影（与 Mapper 列别名一致）。
 *
 * @author Bunny
 */
@Getter
@Setter
public class UserInvalidationStateProjection {

	/**
	 * 用户 ID。
	 */
	private Long userId;

	/**
	 * 用户状态（0=禁用,1=正常,2=锁定）。
	 */
	private Integer status;

	/**
	 * 逻辑删除标记（0=否,1=是）。
	 */
	private Integer isDeleted;

}
