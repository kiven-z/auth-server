package com.auth.service.system.admin.model.form.userdept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增用户部门关联
 * FIXME 考虑合并，分配表单和创建表单、修改表单是同一个，删除UserDeptUpdateForm使用这个
 * FIXME 在表单中添加 userId、deptId
 *
 * @author Bunny
 */
@Schema(name = "UserDeptAssignForm", title = "新增用户部门关联")
@Getter
@Setter
public class UserDeptAssignForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(title = "部门 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "部门必填")
    private Long deptId;

    @Schema(title = "关联状态(0=已调离,1=正常,2=兼职)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态必填")
    private Integer status;

    @Schema(title = "是否主部门；不传时由服务端按规则推断")
    private Boolean isPrimary;

    @Schema(title = "在部门内职务")
    @Size(max = 64, message = "文字长度必须 <= 64")
    private String position;

    @Schema(title = "备注")
    @Size(max = 255, message = "备注长度必须 <= 255")
    private String remark;

}
