package com.auth.module.file.export.shard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ShardedExportSession} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ShardedExportSession 分片会话")
class ShardedExportSessionTest {

	private static void cleanup(List<Path> paths) throws IOException {
		for (Path path : paths) {
			Files.deleteIfExists(path);
		}
	}

	@Test
	@DisplayName("writeRows：跨分片边界时切开续写")
	void writeRows_whenCrossingShardBoundary_splitsIntoMultipleFiles() throws IOException {
		// shardRows=3：一批 5 行应拆成 3+2 两个分片
		RecordingSessionFactory factory = new RecordingSessionFactory();
		ShardedExportSession<String> session = new ShardedExportSession<>(factory, 3);

		session.writeRows(List.of("a", "b", "c", "d", "e"));
		List<Path> paths = session.finish();

		assertThat(paths).hasSize(2);
		assertThat(factory.completedChunks()).containsExactly(List.of("a", "b", "c"), List.of("d", "e"));
		assertThat(paths).allMatch(Files::exists);
		cleanup(paths);
	}

	@Test
	@DisplayName("writeRows：多批写入跨分片时按容量切开")
	void writeRows_whenMultipleBatchesCrossBoundary_splitsCorrectly() throws IOException {
		// 先写 2 行，再写 4 行：第二批应拆成 1+3
		RecordingSessionFactory factory = new RecordingSessionFactory();
		ShardedExportSession<String> session = new ShardedExportSession<>(factory, 3);

		session.writeRows(List.of("a", "b"));
		session.writeRows(List.of("c", "d", "e", "f"));
		List<Path> paths = session.finish();

		assertThat(paths).hasSize(2);
		assertThat(factory.completedChunks()).containsExactly(List.of("a", "b", "c"), List.of("d", "e", "f"));
		cleanup(paths);
	}

	@Test
	@DisplayName("writeRows：shardRows<=0 时不分片")
	void writeRows_whenShardRowsDisabled_keepsSingleFile() throws IOException {
		RecordingSessionFactory factory = new RecordingSessionFactory();
		ShardedExportSession<String> session = new ShardedExportSession<>(factory, 0);

		session.writeRows(List.of("a", "b", "c", "d"));
		List<Path> paths = session.finish();

		assertThat(paths).hasSize(1);
		assertThat(factory.completedChunks()).containsExactly(List.of("a", "b", "c", "d"));
		cleanup(paths);
	}

	@Test
	@DisplayName("finish：无写入时仍产出一个空分片")
	void finish_whenNoRows_producesSingleEmptyShard() throws IOException {
		RecordingSessionFactory factory = new RecordingSessionFactory();
		ShardedExportSession<String> session = new ShardedExportSession<>(factory, 3);

		List<Path> paths = session.finish();

		assertThat(paths).hasSize(1);
		assertThat(factory.openCount()).isEqualTo(1);
		assertThat(factory.completedChunks()).containsExactly(List.of());
		cleanup(paths);
	}

	@Test
	@DisplayName("abortQuietly：清理已完成与进行中的临时文件")
	void abortQuietly_whenPartialShardsExist_deletesTempFiles() throws IOException {
		RecordingSessionFactory factory = new RecordingSessionFactory();
		ShardedExportSession<String> session = new ShardedExportSession<>(factory, 2);

		session.writeRows(List.of("a", "b", "c"));
		List<Path> openedBeforeAbort = List.copyOf(factory.openedPaths());
		assertThat(openedBeforeAbort).isNotEmpty();

		session.abortQuietly();

		assertThat(openedBeforeAbort).allMatch(path -> !Files.exists(path));
		List<String> afterAbortRows = List.of("x");
		assertThatThrownBy(() -> session.writeRows(afterAbortRows)).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("aborted");
	}

	@Test
	@DisplayName("writeRows：写入失败时 abortQuietly 可清理")
	void writeRows_whenSessionFails_abortCleansUp() {
		FailingSessionFactory factory = new FailingSessionFactory(1);
		ShardedExportSession<String> session = new ShardedExportSession<>(factory, 10);
		List<String> rows = List.of("a");

		assertThatThrownBy(() -> session.writeRows(rows)).isInstanceOf(IOException.class).hasMessageContaining("boom");
		session.abortQuietly();

		assertThat(factory.openedPaths()).allMatch(path -> !Files.exists(path));
	}

	private static final class RecordingSessionFactory implements ChunkFileSessionFactory<String> {

		private final List<List<String>> completedChunks = new ArrayList<>();

		private final List<Path> openedPaths = new ArrayList<>();

		private final AtomicInteger openCount = new AtomicInteger();

		@Override
		public ChunkFileSession<String> open() throws IOException {
			openCount.incrementAndGet();
			Path path = Files.createTempFile("shard-test-", ".txt");
			openedPaths.add(path);
			return new RecordingSession(path, completedChunks);
		}

		@Override
		public String fileExtension() {
			return "txt";
		}

		@Override
		public String contentType() {
			return "text/plain";
		}

		List<List<String>> completedChunks() {
			return completedChunks;
		}

		List<Path> openedPaths() {
			return openedPaths;
		}

		int openCount() {
			return openCount.get();
		}

	}

	private static final class RecordingSession implements ChunkFileSession<String> {

		private final Path path;

		private final List<List<String>> completedChunks;

		private final List<String> buffer = new ArrayList<>();

		private boolean completed;

		private boolean closed;

		private RecordingSession(Path path, List<List<String>> completedChunks) {
			this.path = path;
			this.completedChunks = completedChunks;
		}

		@Override
		public void writeChunk(List<String> rows) {
			if (rows != null) {
				buffer.addAll(rows);
			}
		}

		@Override
		public Path complete() throws IOException {
			Files.writeString(path, String.join(",", buffer));
			completedChunks.add(List.copyOf(buffer));
			completed = true;
			return path;
		}

		@Override
		public void close() throws IOException {
			if (completed || closed) {
				return;
			}
			closed = true;
			Files.deleteIfExists(path);
		}

	}

	private static final class FailingSessionFactory implements ChunkFileSessionFactory<String> {

		private final int failOnOpenIndex;

		private final List<Path> openedPaths = new ArrayList<>();

		private int openIndex;

		private FailingSessionFactory(int failOnOpenIndex) {
			this.failOnOpenIndex = failOnOpenIndex;
		}

		@Override
		public ChunkFileSession<String> open() throws IOException {
			openIndex++;
			Path path = Files.createTempFile("shard-fail-", ".txt");
			openedPaths.add(path);
			if (openIndex == failOnOpenIndex) {
				return new FailingSession(path);
			}
			return new RecordingSession(path, new ArrayList<>());
		}

		@Override
		public String fileExtension() {
			return "txt";
		}

		@Override
		public String contentType() {
			return "text/plain";
		}

		List<Path> openedPaths() {
			return openedPaths;
		}

	}

	private record FailingSession(Path path) implements ChunkFileSession<String> {

		@Override
		public void writeChunk(List<String> rows) throws IOException {
			throw new IOException("boom");
		}

		@Override
		public Path complete() {
			return path;
		}

		@Override
		public void close() throws IOException {
			Files.deleteIfExists(path);
		}

	}

}
