package com.auth.service.system.admin.model.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色下拉选项 VO
 *
 * @author Bunny
 */
@Schema(name = "SysRoleOptionVO", title = "角色下拉选项")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysRoleOptionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(title = "主键ID")
    private Long id;

    @Schema(title = "角色编码")
    private String roleCode;

    @Schema(title = "角色名称")
    private String roleName;

}
