package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 菜单与权限编码关联（页面按钮子集），对应 sys_menu_permission
 *
 * @author Bunny
 */
@TableName("sys_menu_permission")
@Schema(name = "SysMenuPermissionEntity", title = "菜单权限关联")
@Getter
@Setter
public class SysMenuPermissionEntity extends BaseEntity {

	@Schema(title = "菜单ID")
	private Long menuId;

	@Schema(title = "权限编码")
	private String permissionCode;

}
