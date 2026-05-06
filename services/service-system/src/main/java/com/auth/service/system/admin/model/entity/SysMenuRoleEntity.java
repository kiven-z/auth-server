package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 菜单与角色编码关联，对应 sys_menu_role
 *
 * @author Bunny
 */
@TableName("sys_menu_role")
@Schema(name = "SysMenuRoleEntity", title = "菜单角色关联")
@Getter
@Setter
public class SysMenuRoleEntity extends BaseEntity {

    @Schema(title = "菜单ID")
    private Long menuId;

    @Schema(title = "角色编码")
    private String roleCode;

}
