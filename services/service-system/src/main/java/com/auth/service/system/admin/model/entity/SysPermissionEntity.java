package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统权限主数据，对应 {@code sys_permission}（与 {@code sys_menu_permission.permission_code} 外键一致）
 *
 * @author Bunny
 */
@TableName("sys_permission")
@Schema(name = "SysPermissionEntity", title = "系统权限")
@Getter
@Setter
public class SysPermissionEntity extends BaseEntity {

	@Schema(title = "权限编码，全局唯一")
	private String permissionCode;

	@Schema(title = "权限名称")
	private String permissionName;

	@Schema(title = "启用状态(true=启用)")
	private Boolean status;

}
