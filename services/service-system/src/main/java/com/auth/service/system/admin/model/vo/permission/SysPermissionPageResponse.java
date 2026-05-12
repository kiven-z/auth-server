package com.auth.service.system.admin.model.vo.permission;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限分页行
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionPageResponse", title = "权限分页行")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysPermissionPageResponse extends BaseResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "权限类型(0=按钮, 1=API)")
	private Integer permissionType;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "启用状态(true=正常启用，false=停用)")
	private Boolean status;

}
