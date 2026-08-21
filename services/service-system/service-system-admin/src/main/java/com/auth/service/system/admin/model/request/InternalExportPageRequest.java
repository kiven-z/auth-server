package com.auth.service.system.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 内部分页取数请求（异步导出回调）
 *
 * @author Bunny
 */
@Schema(name = "InternalExportPageRequest", title = "内部分页取数请求")
@Getter
@Setter
public class InternalExportPageRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
	@Size(max = 64, message = "业务类型长度不能超过64个字符")
	@NotBlank(message = "业务类型不能为空")
	private String bizType;

	@Schema(title = "查询条件快照 JSON", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "查询条件不能为空")
	private String querySnapshotJson;

	@Schema(title = "数据权限快照 JSON", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "权限快照不能为空")
	private String scopeSnapshotJson;

	@Schema(title = "页码（从 1 开始）")
	@Min(value = 1, message = "页码不能小于1")
	private Integer pageNum;

	@Schema(title = "每页条数")
	@Max(value = 10000, message = "每页条数不能大于10000")
	@Min(value = 1, message = "每页条数不能小于1")
	private Integer pageSize;

}
