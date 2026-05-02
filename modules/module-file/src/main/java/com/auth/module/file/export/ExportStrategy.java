package com.auth.module.file.export;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 统一的导出策略接口
 *
 * @param <T> 数据类型
 * @param <R> 返回结果类型
 * @author Bunny
 */
public interface ExportStrategy<T, R> {

	/**
	 * 导出数据
	 * @param data 要导出的数据
	 * @param outputStream 输出流
	 * @param filename 文件名（可选，某些场景可能不需要）
	 * @return 导出结果
	 * @throws IOException 导出过程中的IO异常
	 */
	R export(T data, OutputStream outputStream, String filename) throws IOException;

}