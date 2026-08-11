package com.auth.service.system.file.model.po;

import lombok.Getter;
import lombok.Setter;

/**
 * 文件异步导出过期任务行
 *
 * @author Bunny
 */
@Getter
@Setter
public class FileExportTaskExpireRowPO {

	/**
	 * 任务ID
	 */
	private Long id;

	/**
	 * 导出产物文件记录ID
	 */
	private Long fileRecordId;

}
