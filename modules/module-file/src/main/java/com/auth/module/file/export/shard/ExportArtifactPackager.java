package com.auth.module.file.export.shard;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.export.writer.TempFileSupport;
import com.auth.module.file.export.writer.ZipStreamWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 导出产物打包：单分片透传，多分片打 zip
 *
 * @author Bunny
 */
public final class ExportArtifactPackager {

	private static final String ZIP_EXTENSION = "zip";

	private static final String ZIP_CONTENT_TYPE = "application/zip";

	private static final String ZIP_SUFFIX = ".zip";

	private static final String DEFAULT_FILE_PREFIX = "export";

	private static String formatPartIndex(int partNumber) {
		return String.format(Locale.ROOT, "%03d", partNumber);
	}

	private static String normalizeExtension(String shardFileExtension) {
		String trimmed = CharSequenceUtil.trim(shardFileExtension);
		if (CharSequenceUtil.isBlank(trimmed)) {
			throw new IllegalArgumentException("shardFileExtension must not be blank");
		}
		return trimmed.startsWith(".") ? trimmed.substring(1).toLowerCase(Locale.ROOT)
				: trimmed.toLowerCase(Locale.ROOT);
	}

	private static String resolvePrefix(String filePrefix) {
		String trimmed = CharSequenceUtil.trim(filePrefix);
		return CharSequenceUtil.isBlank(trimmed) ? DEFAULT_FILE_PREFIX : trimmed;
	}

	private static void deleteAllQuietly(List<Path> paths) {
		for (Path path : paths) {
			TempFileSupport.deleteQuietly(path);
		}
	}

	/**
	 * 打包分片产物
	 * @param shardPaths 分片临时文件
	 * @param shardFileExtension 分片扩展名（不含点）
	 * @param shardContentType 分片 MIME（单分片场景直接透传为产物 MIME）
	 * @param filePrefix 文件名前缀
	 * @return 打包产物
	 * @throws IOException 打包失败
	 */
	public PackagedExportArtifact pack(List<Path> shardPaths, String shardFileExtension, String shardContentType,
			String filePrefix) throws IOException {
		if (shardPaths == null || shardPaths.isEmpty()) {
			throw new IllegalArgumentException("shardPaths must not be empty");
		}
		String extension = normalizeExtension(shardFileExtension);
		String prefix = resolvePrefix(filePrefix);
		if (shardPaths.size() == 1) {
			return PackagedExportArtifact.builder()
				.filePath(shardPaths.get(0))
				.fileExtension(extension)
				.contentType(shardContentType)
				.shardCount(1)
				.build();
		}
		return packZip(shardPaths, extension, prefix);
	}

	private PackagedExportArtifact packZip(List<Path> shardPaths, String extension, String prefix) throws IOException {
		Path zipPath = null;
		try {
			List<ZipStreamWriter.ZipEntryPayload> payloads = buildPayloads(shardPaths, extension, prefix);
			zipPath = TempFileSupport.writeToTempFile(ZIP_SUFFIX,
					outputStream -> ZipStreamWriter.write(outputStream, payloads));
			deleteAllQuietly(shardPaths);
			return PackagedExportArtifact.builder()
				.filePath(zipPath)
				.fileExtension(ZIP_EXTENSION)
				.contentType(ZIP_CONTENT_TYPE)
				.shardCount(shardPaths.size())
				.build();
		}
		catch (IOException | RuntimeException exception) {
			TempFileSupport.deleteQuietly(zipPath);
			deleteAllQuietly(shardPaths);
			throw exception;
		}
	}

	private List<ZipStreamWriter.ZipEntryPayload> buildPayloads(List<Path> shardPaths, String extension,
			String prefix) {
		List<ZipStreamWriter.ZipEntryPayload> payloads = new ArrayList<>(shardPaths.size());
		for (int index = 0; index < shardPaths.size(); index++) {
			Path shardPath = shardPaths.get(index);
			String entryName = prefix + "_part" + formatPartIndex(index + 1) + "." + extension;
			payloads.add(new ZipStreamWriter.ZipEntryPayload(entryName, () -> Files.newInputStream(shardPath)));
		}
		return payloads;
	}

}
