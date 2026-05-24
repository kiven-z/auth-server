package com.auth.service.system.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色与权限码关联，对应 {@code sys_role_permission}。
 *
 * @author Bunny
 */
@TableName("sys_role_permission")
@Schema(name = "SysRolePermissionEntity", title = "角色权限关联")
@Getter
@Setter
public class SysRolePermissionEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private Long id;

	@Schema(title = "角色编码")
	private String roleCode;

	@Schema(title = "权限码")
	private String permissionCode;

	@Schema(title = "授权时间")
	private LocalDateTime grantTime;

	@Schema(title = "授权人 userId")
	private Long grantorId;

}
