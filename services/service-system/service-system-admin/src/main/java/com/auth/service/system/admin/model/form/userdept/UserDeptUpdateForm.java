package com.auth.service.system.admin.model.form.userdept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新用户部门关联
 * FIXME 使用UserDeptAssignForm删除这个类
 *
 * @author Bunny
 */
@Schema(name = "UserDeptUpdateForm", title = "更新用户部门关联")
@Getter
@Setter
public class UserDeptUpdateForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(title = "关联状态(0=已调离,1=正常,2=兼职)", requiredMode = Schema.RequiredMode.REQUIRED)
    @Max(value = 2, message = "状态值必须 <= 2")
    @Min(value = 0, message = "状态值必须 >= 0")
    @NotNull(message = "status is required")
    private Integer status;

    @Schema(title = "是否主部门")
    private Boolean isPrimary;

    @Schema(title = "在部门内职务")
    @Size(max = 64, message = "文字长度必须 <= 64")
    private String position;

    @Schema(title = "备注")
    @Size(max = 255, message = "备注长度必须 <= 255")
    private String remark;

    // FIXME 需要删除这个
    @Schema(title = "乐观锁版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "version 必填")
    private Long version;

}
