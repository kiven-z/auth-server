package com.auth.module.file.api.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 异步导出任务创建结果
 *
 * @author Bunny
 */
@Getter
@Setter
public class FileExportTaskCreateResult implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 任务 ID
	 */
	private Long taskId;

}
