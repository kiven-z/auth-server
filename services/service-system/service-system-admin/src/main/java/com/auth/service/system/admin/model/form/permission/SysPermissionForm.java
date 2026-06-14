package com.auth.service.system.admin.model.form.permission;

import com.auth.common.web.valid.group.CreateGroup;
import com.auth.common.web.valid.group.UpdateGroup;
import com.auth.module.security.contract.convention.AuthCodeConvention;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限新增/更新表单
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionForm", title = "权限保存表单")
@Getter
@Setter
public class SysPermissionForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "权限主键，更新时必填")
	@NotNull(groups = UpdateGroup.class, message = "权限ID不能为空")
	private Long id;

	@Schema(title = "权限编码（唯一标识）", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 128, message = "权限编码长度不能超过128个字符", groups = { CreateGroup.class, UpdateGroup.class })
	@Pattern(regexp = AuthCodeConvention.PERMISSION_CODE_REGEX,
			message = "权限编码格式不符合约定（全小写，1～3 段以冒号分隔，每段为 * 或小写 [a-z][a-z0-9]*）",
			groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "权限编码不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String permissionCode;

	@Schema(title = "权限名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 128, message = "权限名称长度不能超过128个字符", groups = { CreateGroup.class, UpdateGroup.class })
	@NotBlank(message = "权限名称不能为空", groups = { CreateGroup.class, UpdateGroup.class })
	private String permissionName;

	@Schema(title = "权限类型(0=按钮, 1=API)，仅创建时有效", requiredMode = Schema.RequiredMode.REQUIRED)
	@Max(value = 1, message = "权限类型值无效", groups = CreateGroup.class)
	@Min(value = 0, message = "权限类型值无效", groups = CreateGroup.class)
	@NotNull(message = "权限类型不能为空", groups = CreateGroup.class)
	private Integer permissionType;

	@Schema(title = "显示顺序")
	private Integer orderNum;

	@Schema(title = "启用状态(true=启用，false=停用)；创建时不传默认启用，更新时必填")
	@NotNull(message = "状态不能为空", groups = UpdateGroup.class)
	private Boolean status;

	@Schema(title = "备注")
	@Size(max = 500, message = "备注长度不能超过500个字符", groups = { CreateGroup.class, UpdateGroup.class })
	private String remark;

}
