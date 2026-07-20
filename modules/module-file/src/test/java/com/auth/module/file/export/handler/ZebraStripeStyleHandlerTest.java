package com.auth.module.file.export.handler;

import com.alibaba.excel.annotation.ExcelProperty;
import com.auth.module.file.export.model.enums.ZebraStripeStyleType;
import com.auth.module.file.export.writer.ExcelExportWriter;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ZebraStripeStyleHandler} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ZebraStripeStyleHandler 斑马纹样式")
class ZebraStripeStyleHandlerTest {

	/**
	 * BLUE_GREY_TECH 表头偶数列背景色
	 */
	private static final byte[] EXPECTED_HEADER_EVEN_RGB = new byte[] { (byte) 140, (byte) 160, (byte) 180 };

	private static byte[] exportWithZebraTheme(List<ZebraExportTestRow> rows) throws IOException {
		return new ExcelExportWriter<>(ZebraExportTestRow.class, "测试")
			.registerWriteHandler(ZebraStripeStyleType.getHandler(ZebraStripeStyleType.BLUE_GREY_TECH))
			.write(rows);
	}

	private static void assertHeaderStyleIndicesValid(byte[] bytes) throws IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
			Sheet sheet = workbook.getSheetAt(0);
			Row header = sheet.getRow(0);
			assertThat(header).isNotNull();

			int styleCount = workbook.getNumCellStyles();
			for (int col = 0; col < 2; col++) {
				Cell cell = header.getCell(col);
				assertThat(cell).isNotNull();
				assertThat((int) cell.getCellStyle().getIndex()).isLessThan(styleCount);
			}
		}
	}

	private static byte[] readHeaderEvenColumnRgb(byte[] bytes) throws IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
			XSSFCellStyle style = workbook.getSheetAt(0).getRow(0).getCell(0).getCellStyle();
			return style.getFillForegroundColorColor().getRGB();
		}
	}

	@Test
	@DisplayName("同一处理器连续两次写出时表头样式不串用其他工作簿")
	void writeTwice_withSameHandler_headerStylesStayConsistent() throws IOException {
		// 模拟导出服务：构造一次 writer + handler，多次 write 产生不同 Workbook
		ZebraStripeStyleHandler handler = new ZebraStripeStyleHandler(ZebraStripeStyleType.BLUE_GREY_TECH.getStyle());
		ExcelExportWriter<ZebraExportTestRow> writer = new ExcelExportWriter<>(ZebraExportTestRow.class, "测试")
			.registerWriteHandler(handler);

		ZebraExportTestRow row = new ZebraExportTestRow();
		row.setName("样例");
		row.setCode("CODE-001");

		byte[] firstBytes = writer.write(List.of(row));
		byte[] secondBytes = writer.write(List.of(row));

		assertThat(readHeaderEvenColumnRgb(secondBytes)).isEqualTo(readHeaderEvenColumnRgb(firstBytes));
		assertThat(readHeaderEvenColumnRgb(secondBytes)).isEqualTo(EXPECTED_HEADER_EVEN_RGB);
		assertHeaderStyleIndicesValid(secondBytes);
	}

	@Test
	@DisplayName("连续两次独立导出时表头背景色保持一致")
	void writeTwice_withFreshHandlers_headerColorsMatchTheme() throws IOException {
		ZebraExportTestRow row = new ZebraExportTestRow();
		row.setName("样例");
		row.setCode("CODE-001");

		byte[] firstBytes = exportWithZebraTheme(List.of(row));
		byte[] secondBytes = exportWithZebraTheme(List.of(row));

		assertThat(readHeaderEvenColumnRgb(firstBytes)).isEqualTo(EXPECTED_HEADER_EVEN_RGB);
		assertThat(readHeaderEvenColumnRgb(secondBytes)).isEqualTo(EXPECTED_HEADER_EVEN_RGB);
	}

	@Getter
	@Setter
	private static class ZebraExportTestRow {

		@ExcelProperty(value = "编码", index = 0)
		private String code;

		@ExcelProperty(value = "名称", index = 1)
		private String name;

	}

}
