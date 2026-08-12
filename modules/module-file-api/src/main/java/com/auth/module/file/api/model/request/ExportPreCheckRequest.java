package com.auth.module.file.api.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 导出前置检查请求
 *
 * @author Bunny
 */
@Getter
@Setter
public class ExportPreCheckRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 业务类型
	 */
	@Size(max = 64, message = "业务类型长度不能超过64个字符")
	@NotBlank(message = "业务类型不能为空")
	private String bizType;

	/**
	 * 分页取数目标服务名
	 */
	@Size(max = 64, message = "取数服务长度不能超过64个字符")
	@NotBlank(message = "取数服务不能为空")
	private String fetchService;

	/**
	 * 分页取数路径
	 */
	@Size(max = 256, message = "取数路径长度不能超过256个字符")
	@NotBlank(message = "取数路径不能为空")
	private String fetchPath;

	/**
	 * 查询条件快照 JSON
	 */
	@NotBlank(message = "查询条件不能为空")
	private String querySnapshotJson;

}
