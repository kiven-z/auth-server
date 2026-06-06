package com.auth.service.system.admin.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionQuery", title = "权限分页查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysPermissionQuery extends PageQueryRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限编码（模糊）")
	private String permissionCode;

	@Schema(title = "权限名称（模糊）")
	private String permissionName;

	@Schema(title = "权限类型(0=按钮, 1=API)")
	private Integer permissionType;

	@Schema(title = "启用状态(true=正常启用，false=停用)")
	private Boolean status;

}
