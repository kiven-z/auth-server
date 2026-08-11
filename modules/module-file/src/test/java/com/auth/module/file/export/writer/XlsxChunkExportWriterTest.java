package com.auth.module.file.export.writer;

import com.auth.module.file.export.definition.ExportColumn;
import com.auth.module.file.export.definition.ExportSheetMeta;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XlsxChunkExportWriter} 单元测试
 *
 * @author Bunny
 */
@DisplayName("XlsxChunkExportWriter Map 动态表头写入")
class XlsxChunkExportWriterTest {

	@Test
	@DisplayName("write：写入表头与数据单元格")
	void write_whenMapRows_writesHeaderAndCells() throws IOException {
		ExportSheetMeta sheetMeta = ExportSheetMeta.builder()
			.sheetName("订单")
			.columns(List.of(ExportColumn.builder().fieldKey("id").header("主键").build(),
					ExportColumn.builder().fieldKey("title").header("标题").build()))
			.build();
		XlsxChunkExportWriter writer = new XlsxChunkExportWriter(sheetMeta);
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("id", 1001L);
		row.put("title", "演示单");

		byte[] bytes = writer.write(List.of(row));

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();
			Row header = sheet.getRow(0);
			Row data = sheet.getRow(1);
			assertThat(formatter.formatCellValue(header.getCell(0))).isEqualTo("主键");
			assertThat(formatter.formatCellValue(header.getCell(1))).isEqualTo("标题");
			assertThat(formatter.formatCellValue(data.getCell(0))).isEqualTo("1001");
			assertThat(formatter.formatCellValue(data.getCell(1))).isEqualTo("演示单");
		}
	}

}
