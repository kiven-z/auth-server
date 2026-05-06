package com.auth.service.system.admin.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "SysRoleQuery", title = "角色分页查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码（模糊）")
	private String roleCode;

	@Schema(title = "角色名称（模糊）")
	private String roleName;

	@Schema(title = "状态(0=正常，1=禁用)")
	private Integer status;

	@Schema(title = "角色类型(1-4)")
	private Integer roleType;

}
