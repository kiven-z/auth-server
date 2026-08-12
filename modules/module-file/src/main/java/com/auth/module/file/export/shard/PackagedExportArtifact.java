package com.auth.module.file.export.shard;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;

/**
 * 打包后的导出产物
 *
 * @author Bunny
 */
@Value
@Builder
public class PackagedExportArtifact {

	/**
	 * 产物临时文件路径
	 */
	Path filePath;

	/**
	 * 扩展名（不含点）
	 */
	String fileExtension;

	/**
	 * MIME
	 */
	String contentType;

	/**
	 * 分片数（单文件为 1）
	 */
	int shardCount;

}
