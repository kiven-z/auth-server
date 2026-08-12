package com.auth.module.file.export.shard;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 单个物理导出文件会话
 *
 * @param <R> 导出行类型
 * @author Bunny
 */
public interface ChunkFileSession<R> extends Closeable {

	/**
	 * 写入一块数据
	 * @param rows 导出行
	 * @throws IOException 写入失败
	 */
	void writeChunk(List<R> rows) throws IOException;

	/**
	 * 完成写入并返回临时文件路径
	 * @return 临时文件路径
	 * @throws IOException 完成失败
	 */
	Path complete() throws IOException;

}
