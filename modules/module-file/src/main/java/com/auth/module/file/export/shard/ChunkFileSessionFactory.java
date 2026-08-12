package com.auth.module.file.export.shard;

import java.io.IOException;

/**
 * 单文件导出会话工厂
 *
 * @param <R> 导出行类型
 * @author Bunny
 */
public interface ChunkFileSessionFactory<R> {

	/**
	 * 打开新的单文件会话
	 * @return 会话
	 * @throws IOException 打开失败
	 */
	ChunkFileSession<R> open() throws IOException;

	/**
	 * @return 分片文件扩展名（不含点）
	 */
	String fileExtension();

	/**
	 * @return MIME
	 */
	String contentType();

}
