package com.auth.module.file.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * Zip压缩包导出策略基类,用于包装具体的文件导出策略到Zip中
 *
 * @author Bunny
 */
public abstract class AbstractZipExportStrategy<T> implements ExportStrategy<T, Void> {

	/**
	 * 获取文件名
	 * @param data 读取到的列表数据
	 * @param baseFilename 基础文件名
	 * @return 文件名
	 */
	protected abstract String getFilename(T data, String baseFilename);

	/**
	 * 获取文件内容
	 * @param data 读取到的列表数据
	 * @return 文件内容
	 * @throws IOException 获取文件内容时发生IO异常
	 */
	protected abstract byte[] getFileContent(T data) throws IOException;

	@Override
	public Void export(T data, OutputStream outputStream, String filename) throws IOException {
		if (!(outputStream instanceof ZipOutputStream zipOutputStream)) {
			throw new IllegalArgumentException("Zip 导出需要使用ZipOutputStream");
		}

		String entryFilename = getFilename(data, filename);
		byte[] content = getFileContent(data);

		try {
			var zipEntry = new java.util.zip.ZipEntry(entryFilename);
			zipOutputStream.putNextEntry(zipEntry);
			zipOutputStream.write(content);
			zipOutputStream.closeEntry();
		}
		catch (Exception e) {
			throw new IOException("Zip 条目写入失败", e);
		}

		return null;
	}

}