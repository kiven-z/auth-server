package com.auth.service.system.admin.model.query.permission;

import com.auth.module.file.export.ExportScopeQuery;
import com.auth.module.file.export.model.enums.ExportScope;
import com.auth.service.system.admin.model.query.SysPermissionQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 权限 Excel 导出查询（筛选与分页列表一致，并指定导出范围）
 *
 * @author Bunny
 */
@Schema(name = "SysPermissionExportQuery", title = "权限导出查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysPermissionExportQuery extends SysPermissionQuery implements ExportScopeQuery {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "导出范围；未传时按 PAGE_RANGE 处理", example = "PAGE_RANGE")
	private ExportScope exportScope;

	@Schema(title = "起始页码（1-based，含；PAGE_RANGE 时必填）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
	@Min(value = 1, message = "起始页码不能小于1")
	@NotNull(message = "起始页码不能为空")
	private Integer fromPage;

	@Schema(title = "结束页码（1-based，含；PAGE_RANGE 时必填）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
	@Min(value = 1, message = "结束页码不能小于1")
	@NotNull(message = "结束页码不能为空")
	private Integer toPage;

}
