package com.auth.service.system.admin.model.vo.permission;

import com.auth.common.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.List;

/**
 * 权限详情 VO
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionDetailVO", title = "权限详情")
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SysPermissionDetailVO extends BaseResponse {

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

    @Schema(title = "启用状态(true=启用，false=停用)")
    private Boolean status;

    @Schema(title = "备注")
    private String remark;

    @Schema(title = "已绑定角色列表（sys_role_permission 反查）")
    private List<SysPermissionBoundRoleItemVO> boundRoles;

    @Schema(title = "已绑定菜单列表（sys_menu_permission 反查")
    private List<SysPermissionBoundMenuItemVO> boundMenus;

}
