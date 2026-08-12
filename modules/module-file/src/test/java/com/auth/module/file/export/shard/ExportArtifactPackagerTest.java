package com.auth.module.file.export.shard;

import com.auth.module.file.export.writer.XlsxChunkFileSessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ExportArtifactPackager} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExportArtifactPackager 产物打包")
class ExportArtifactPackagerTest {

	private final ExportArtifactPackager packager = new ExportArtifactPackager();

	@Test
	@DisplayName("pack：单分片直接透传")
	void pack_whenSingleShard_passthrough() throws IOException {
		Path shard = Files.createTempFile("pack-single-", ".xlsx");
		Files.writeString(shard, "single");

		PackagedExportArtifact artifact = packager.pack(List.of(shard), "xlsx",
				XlsxChunkFileSessionFactory.CONTENT_TYPE, "orders");

		assertThat(artifact.getFilePath()).isEqualTo(shard);
		assertThat(artifact.getFileExtension()).isEqualTo("xlsx");
		assertThat(artifact.getContentType()).isEqualTo(XlsxChunkFileSessionFactory.CONTENT_TYPE);
		assertThat(artifact.getShardCount()).isEqualTo(1);
		assertThat(Files.exists(shard)).isTrue();
		Files.deleteIfExists(shard);
	}

	@Test
	@DisplayName("pack：多分片打 zip 并删除源分片")
	void pack_whenMultipleShards_createsZipAndDeletesSources() throws IOException {
		Path shard1 = Files.createTempFile("pack-multi-1-", ".xlsx");
		Path shard2 = Files.createTempFile("pack-multi-2-", ".xlsx");
		Files.writeString(shard1, "part-1");
		Files.writeString(shard2, "part-2");

		PackagedExportArtifact artifact = packager.pack(List.of(shard1, shard2), "xlsx",
				XlsxChunkFileSessionFactory.CONTENT_TYPE, "orders");

		assertThat(artifact.getFileExtension()).isEqualTo("zip");
		assertThat(artifact.getContentType()).isEqualTo("application/zip");
		assertThat(artifact.getShardCount()).isEqualTo(2);
		assertThat(Files.exists(shard1)).isFalse();
		assertThat(Files.exists(shard2)).isFalse();
		assertThat(Files.exists(artifact.getFilePath())).isTrue();

		List<String> entryNames = readEntryNames(Files.readAllBytes(artifact.getFilePath()));
		assertThat(entryNames).containsExactly("orders_part001.xlsx", "orders_part002.xlsx");
		Files.deleteIfExists(artifact.getFilePath());
	}

	@Test
	@DisplayName("pack：打包失败时清理分片文件")
	void pack_whenZipFails_cleansShardFiles() throws IOException {
		Path missing = Path.of("definitely-missing-shard-" + System.nanoTime() + ".xlsx");
		Path existing = Files.createTempFile("pack-fail-", ".xlsx");
		Files.writeString(existing, "keep-me");

		assertThatThrownBy(() -> packager.pack(List.of(existing, missing), "xlsx",
				XlsxChunkFileSessionFactory.CONTENT_TYPE, "orders"))
			.isInstanceOf(IOException.class);

		assertThat(Files.exists(existing)).isFalse();
	}

	@Test
	@DisplayName("pack：空分片列表拒绝")
	void pack_whenEmptyShards_throws() {
		List<Path> emptyShards = List.of();
		assertThatThrownBy(() -> packager.pack(emptyShards, "xlsx", XlsxChunkFileSessionFactory.CONTENT_TYPE, "orders"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("shardPaths");
	}

	private List<String> readEntryNames(byte[] zipBytes) throws IOException {
		List<String> entryNames = new ArrayList<>();
		try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				entryNames.add(entry.getName());
			}
		}
		return entryNames;
	}

}
