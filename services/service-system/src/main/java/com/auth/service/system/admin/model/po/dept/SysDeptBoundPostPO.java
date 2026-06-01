package com.auth.service.system.admin.model.po.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门详情-下属岗位行
 */
@Schema(name = "SysDeptBoundPostPO", title = "部门下属岗位 PO")
@Getter
@Setter
public class SysDeptBoundPostPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(title = "岗位ID")
    private Long id;

    @Schema(title = "岗位编码")
    private String postCode;

    @Schema(title = "岗位名称")
    private String postName;

    @Schema(title = "启用状态(true=启用，false=停用)")
    private Boolean status;

    @Schema(title = "显示顺序")
    private Integer orderNum;

}
