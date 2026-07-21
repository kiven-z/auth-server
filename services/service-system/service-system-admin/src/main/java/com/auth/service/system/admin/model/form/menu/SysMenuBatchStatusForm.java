package com.auth.service.system.admin.model.form.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 菜单批量状态
 *
 * @author Bunny
 */
@Schema(name = "SysMenuBatchStatusForm", title = "菜单批量状态")
@Getter
@Setter
public class SysMenuBatchStatusForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "菜单主键列表", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotEmpty(message = "ids must not be empty")
	private List<Long> ids;

	@Schema(title = "目标状态：0 启用 1 禁用", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "status is required")
	private Boolean status;

}
