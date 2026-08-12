package com.auth.module.file.export.writer;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 临时文件写入共用骨架
 *
 * @author Bunny
 */
@UtilityClass
public class TempFileSupport {

	private static final String TEMP_FILE_PREFIX = "file-export-task-";

	/**
	 * 创建临时文件并写入；失败时删除临时文件
	 * @param suffix 文件后缀（含点，如 {@code .xlsx}）
	 * @param action 写入动作
	 * @return 临时文件路径
	 * @throws IOException 写入失败
	 */
	@SuppressWarnings("java:S5443")
	public static Path writeToTempFile(String suffix, OutputStreamAction action) throws IOException {
		Path tempFile = Files.createTempFile(TEMP_FILE_PREFIX, suffix);
		boolean success = false;
		try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
			action.write(outputStream);
			success = true;
			return tempFile;
		}
		finally {
			if (!success) {
				deleteQuietly(tempFile);
			}
		}
	}

	/**
	 * 创建空临时文件（调用方负责写入与失败清理）
	 * @param suffix 文件后缀（含点）
	 * @return 临时文件路径
	 * @throws IOException 创建失败
	 */
	@SuppressWarnings("java:S5443")
	public static Path createTempFile(String suffix) throws IOException {
		return Files.createTempFile(TEMP_FILE_PREFIX, suffix);
	}

	/**
	 * 静默删除临时文件
	 * @param path 文件路径（可空）
	 */
	public static void deleteQuietly(Path path) {
		if (path == null) {
			return;
		}
		try {
			Files.deleteIfExists(path);
		}
		catch (IOException ignored) {
			// best-effort cleanup
		}
	}

	/**
	 * 临时文件输出流写入动作
	 */
	@FunctionalInterface
	public interface OutputStreamAction {

		/**
		 * 向输出流写入
		 * @param outputStream 输出流
		 * @throws IOException 写入失败
		 */
		void write(OutputStream outputStream) throws IOException;

	}

}
