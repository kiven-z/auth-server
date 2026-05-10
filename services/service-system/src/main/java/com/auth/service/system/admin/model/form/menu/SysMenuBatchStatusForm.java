package com.auth.service.system.admin.model.form.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 菜单批量状态
 *
 * @author Bunny
 */
@Schema(name = "SysMenuBatchStatusForm", title = "菜单批量状态")
@Data
public class SysMenuBatchStatusForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "菜单主键列表（字符串 Long，与前端一致）", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotEmpty(message = "ids must not be empty")
	private List<Long> ids;

	/**
	 * 0=启用 1=禁用（与前端约定一致）
	 */
	@Schema(title = "目标状态：0 启用 1 禁用", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "status is required")
	private Boolean status;

}
