package com.auth.service.system.admin.model.query.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 岗位 Excel 导出查询（筛选与分页列表一致，并指定页码区间）
 *
 * @author Bunny
 */
@Schema(name = "SysPostExportQuery", title = "岗位导出查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SysPostExportQuery extends SysPostQuery {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "起始页码（1-based，含）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
	@Min(value = 1, message = "起始页码不能小于1")
	@NotNull(message = "起始页码不能为空")
	private Integer fromPage;

	@Schema(title = "结束页码（1-based，含）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
	@Min(value = 1, message = "结束页码不能小于1")
	@NotNull(message = "结束页码不能为空")
	private Integer toPage;

}
