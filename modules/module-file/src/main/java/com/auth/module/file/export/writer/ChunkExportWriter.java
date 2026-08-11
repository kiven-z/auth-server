package com.auth.module.file.export.writer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 分块导出写入器
 *
 * @param <R> 导出行类型
 * @author Bunny
 */
public interface ChunkExportWriter<R> {

	/**
	 * 一次性写入并返回字节数组
	 * @param rows 导出行列表
	 * @return 导出字节
	 * @throws IOException 写入失败
	 */
	byte[] write(List<R> rows) throws IOException;

	/**
	 * 分块写入临时文件并返回路径
	 * @param writeAction 分块写入动作
	 * @return 临时文件路径
	 * @throws IOException 写入失败
	 */
	Path writeToTempFile(ChunkWriteAction<R> writeAction) throws IOException;

	/**
	 * 分块写入动作
	 *
	 * @param <R> 导出行类型
	 */
	@FunctionalInterface
	interface ChunkWriteAction<R> {

		/**
		 * 执行分块写入动作
		 * @param chunkWriter 分块写入器
		 * @throws IOException 写入失败
		 */
		void write(ChunkWriter<R> chunkWriter) throws IOException;

	}

	/**
	 * 分块写入器
	 *
	 * @param <R> 导出行类型
	 */
	@FunctionalInterface
	interface ChunkWriter<R> {

		/**
		 * 写入一块数据
		 * @param rows 导出行列表
		 * @throws IOException 写入失败
		 */
		void writeChunk(List<R> rows) throws IOException;

	}

}
