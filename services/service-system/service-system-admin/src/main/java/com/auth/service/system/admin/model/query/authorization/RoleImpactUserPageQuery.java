package com.auth.service.system.admin.model.query.authorization;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色影响用户分页查询
 *
 * @author Bunny
 */
@Schema(name = "RoleImpactUserPageQuery", title = "角色影响用户分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RoleImpactUserPageQuery extends PageQueryRequest {

	@Schema(title = "关键词（用户名/昵称）")
	private String keyword;

	@Schema(title = "用户状态", description = "0=禁用，1=正常，2=锁定")
	private Integer status;

}
