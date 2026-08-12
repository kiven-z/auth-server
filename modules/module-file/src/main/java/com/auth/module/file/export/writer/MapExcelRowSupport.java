package com.auth.module.file.export.writer;

import com.auth.module.file.exception.ExportColumnsRequiredException;
import com.auth.module.file.export.definition.ExportColumn;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Map 导出行转换（动态表头与单元格值）
 *
 * @author Bunny
 */
@UtilityClass
public class MapExcelRowSupport {

	/**
	 * 解析列定义（必须显式配置，禁止从 Map 键推断）
	 * @param configuredColumns 显式列定义
	 * @return 列定义
	 */
	public static List<ExportColumn> resolveColumns(List<ExportColumn> configuredColumns) {
		if (configuredColumns == null || configuredColumns.isEmpty()) {
			throw new ExportColumnsRequiredException();
		}

		return List.copyOf(configuredColumns);
	}

	/**
	 * 构建 EasyExcel 动态表头
	 * @param columns 列定义
	 * @return 表头
	 */
	public static List<List<String>> buildHead(List<ExportColumn> columns) {
		List<List<String>> head = new ArrayList<>(columns.size());
		for (ExportColumn column : columns) {
			head.add(List.of(Objects.requireNonNullElse(column.getHeader(), column.getFieldKey())));
		}
		return head;
	}

	/**
	 * 将 Map 行转为按列顺序排列的单元格值
	 * @param rows Map 行
	 * @param columns 列定义
	 * @return EasyExcel 行数据
	 */
	public static List<List<Object>> toCellRows(List<Map<String, Object>> rows, List<ExportColumn> columns) {
		List<List<Object>> cellRows = new ArrayList<>(rows.size());
		for (Map<String, Object> row : rows) {
			cellRows.add(toCellRow(row, columns));
		}
		return cellRows;
	}

	private static List<Object> toCellRow(Map<String, Object> row, List<ExportColumn> columns) {
		List<Object> cells = new ArrayList<>(columns.size());
		Map<String, Object> safeRow = row == null ? Map.of() : row;
		for (ExportColumn column : columns) {
			cells.add(normalizeCellValue(safeRow.get(column.getFieldKey())));
		}
		return cells;
	}

	/**
	 * 规范化单元格值，避免 Long 精度丢失并保证时间可读
	 * @param value 原始值
	 * @return 写入值
	 */
	static Object normalizeCellValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Long longValue) {
			return Long.toString(longValue);
		}
		if (value instanceof Instant instant) {
			return instant.toString();
		}
		if (value instanceof TemporalAccessor temporalAccessor) {
			return temporalAccessor.toString();
		}
		return value;
	}

}
