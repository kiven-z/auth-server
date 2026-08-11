package com.auth.module.file.export.task;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;

/**
 * 异步导出产物
 *
 * @author Bunny
 */
@Value
@Builder
public class FileExportTaskArtifact {

	/**
	 * 文件名
	 */
	String fileName;

	/**
	 * MIME 类型
	 */
	String contentType;

	/**
	 * 临时文件路径
	 */
	Path filePath;

	/**
	 * 导出总行数
	 */
	long rowCount;

}
