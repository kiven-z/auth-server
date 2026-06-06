package com.auth.service.system.admin.model.vo.role;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色详情-已分配权限项
 *
 * @author Bunny
 */
@Schema(name = "SysRoleBoundPermissionItemVO", title = "角色已分配权限项")
@Getter
@Setter
@ToString
public class SysRoleBoundPermissionItemVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限编码")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "权限类型(0=按钮, 1=API)")
	private Integer permissionType;

	@Schema(title = "启用状态(true=启用，false=停用)")
	private Boolean status;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(title = "授权时间")
	private LocalDateTime grantTime;

}
