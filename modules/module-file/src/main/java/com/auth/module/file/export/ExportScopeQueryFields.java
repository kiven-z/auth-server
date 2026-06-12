package com.auth.module.file.export;

import com.auth.module.file.export.model.enums.ExportScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 同步导出共用的范围字段
 *
 * @author Bunny
 */
@Schema(title = "导出范围参数")
@Getter
@Setter
@EqualsAndHashCode
public class ExportScopeQueryFields implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "导出范围；未传时按 PAGE_RANGE 处理", example = "PAGE_RANGE")
	private ExportScope exportScope;

	@Schema(title = "起始页码（1-based，含；PAGE_RANGE 时必填）", example = "1")
	@Min(value = 1, message = "起始页码不能小于1")
	private Integer fromPage;

	@Schema(title = "结束页码（1-based，含；PAGE_RANGE 时必填）", example = "1")
	@Min(value = 1, message = "结束页码不能小于1")
	private Integer toPage;

}
