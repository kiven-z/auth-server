package com.auth.module.file.export.writer;

import com.auth.module.file.exception.ExportValidationException;
import com.auth.module.file.export.definition.ExportColumn;
import com.auth.module.file.export.definition.ExportSheetMeta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ExportWriterFactory} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExportWriterFactory 格式分配")
class ExportWriterFactoryTest {

	private final ExportWriterFactory exportWriterFactory = new ExportWriterFactory();

	@Test
	@DisplayName("create：XLSX 格式返回可写入 Map writer")
	void create_whenXlsx_returnsWritableMapWriter() throws IOException {
		ExportSheetMeta sheetMeta = ExportSheetMeta.builder()
			.sheetName("测试")
			.taskFilePrefix("test")
			.columns(List.of(ExportColumn.builder().fieldKey("id").header("id").build(),
					ExportColumn.builder().fieldKey("name").header("name").build()))
			.build();
		ExportWriterFactory.ExportWriterHandle<Map<String, Object>> handle = exportWriterFactory.create("XLSX",
				sheetMeta);

		byte[] bytes = handle.writer().write(List.of(Map.of("id", 1, "name", "a")));

		assertThat(bytes).isNotEmpty();
		assertThat(handle.fileExtension()).isEqualTo("xlsx");
		assertThat(handle.contentType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	}

	@Test
	@DisplayName("create：暂不支持的格式抛异常")
	void create_whenFormatUnsupported_throwsException() {
		ExportSheetMeta sheetMeta = ExportSheetMeta.builder()
			.sheetName("测试")
			.taskFilePrefix("test")
			.columns(List.of(ExportColumn.builder().fieldKey("id").header("id").build()))
			.build();

		assertThatThrownBy(() -> exportWriterFactory.create("CSV", sheetMeta))
			.isInstanceOf(ExportValidationException.class)
			.hasMessageContaining("导出格式非法");
	}

	@Test
	@DisplayName("create：缺少列定义时抛校验异常")
	void create_whenColumnsMissing_throwsValidationException() {
		ExportSheetMeta sheetMeta = ExportSheetMeta.builder().sheetName("测试").taskFilePrefix("test").build();

		assertThatThrownBy(() -> exportWriterFactory.create("XLSX", sheetMeta))
			.isInstanceOf(ExportValidationException.class)
			.hasMessageContaining("导出列定义不能为空");
	}

}
