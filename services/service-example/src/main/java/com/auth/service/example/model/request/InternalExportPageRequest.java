package com.auth.service.example.model.request;

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
@Getter
@Setter
public class InternalExportPageRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 业务类型
	 */
	@Size(max = 64, message = "业务类型长度不能超过64个字符")
	@NotBlank(message = "业务类型不能为空")
	private String bizType;

	/**
	 * 查询条件快照 JSON
	 */
	@NotBlank(message = "查询条件不能为空")
	private String querySnapshotJson;

	/**
	 * 数据权限快照 JSON
	 */
	@NotBlank(message = "权限快照不能为空")
	private String scopeSnapshotJson;

	/**
	 * 页码（1-based）
	 */
	@Min(value = 1, message = "页码不能小于1")
	private Integer pageNum;

	/**
	 * 每页条数
	 */
	@Max(value = 10000, message = "每页条数不能大于10000")
	@Min(value = 1, message = "每页条数不能小于1")
	private Integer pageSize;

}
