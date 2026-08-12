package com.auth.module.file.export.definition;

import com.alibaba.excel.annotation.ExcelProperty;
import com.auth.common.core.utils.JsonSupport;
import com.auth.module.file.exception.ExportColumnSnapshotInvalidException;
import com.auth.module.file.exception.ExportColumnsRequiredException;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 导出列定义组装与快照编解码
 *
 * @author Bunny
 */
@UtilityClass
public class ExportColumns {

	/**
	 * 由行类型上的 {@link ExcelProperty} 生成列定义（按 index，未指定 index 按声明序）
	 * @param rowType 含 ExcelProperty 的行类型
	 * @return 列定义
	 */
	public static List<ExportColumn> fromExcelProperties(Class<?> rowType) {
		if (rowType == null) {
			throw new IllegalArgumentException("rowType");
		}
		List<IndexedColumn> indexed = new ArrayList<>();
		int declarationOrder = 0;
		for (Field field : rowType.getDeclaredFields()) {
			ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
			if (excelProperty == null) {
				continue;
			}
			String header = resolveHeader(excelProperty, field.getName());
			int index = excelProperty.index();
			indexed.add(new IndexedColumn(index >= 0 ? index : declarationOrder,
					ExportColumn.builder().fieldKey(field.getName()).header(header).build()));
			declarationOrder++;
		}
		if (indexed.isEmpty()) {
			throw new ExportColumnsRequiredException();
		}
		indexed.sort(Comparator.comparingInt(IndexedColumn::index));
		List<ExportColumn> columns = new ArrayList<>(indexed.size());
		for (IndexedColumn item : indexed) {
			columns.add(item.column());
		}
		return List.copyOf(columns);
	}

	/**
	 * 序列化为任务列快照 JSON
	 * @param columns 列定义
	 * @return JSON 数组
	 */
	public static String toSnapshotJson(List<ExportColumn> columns) {
		if (columns == null || columns.isEmpty()) {
			throw new ExportColumnsRequiredException();
		}

		return JsonSupport.toJson(columns);
	}

	/**
	 * 由列快照 JSON 反序列化
	 * @param columnSnapshotJson 列快照
	 * @return 列定义
	 */
	public static List<ExportColumn> fromSnapshotJson(String columnSnapshotJson) {
		if (columnSnapshotJson == null || columnSnapshotJson.isBlank()) {
			throw new ExportColumnsRequiredException();
		}
		List<ExportColumn> columns;
		try {
			columns = JsonSupport.fromJson(columnSnapshotJson, List.class, ExportColumn.class);
		}
		catch (IllegalArgumentException exception) {
			throw new ExportColumnSnapshotInvalidException();
		}

		if (columns == null || columns.isEmpty()) {
			throw new ExportColumnsRequiredException();
		}

		for (ExportColumn column : columns) {
			if (column == null || column.getFieldKey() == null || column.getFieldKey().isBlank()) {
				throw new ExportColumnSnapshotInvalidException();
			}
		}
		return List.copyOf(columns);
	}

	/**
	 * 解析列头
	 * @param excelProperty 列属性
	 * @param fieldName 字段名
	 * @return 列头
	 */
	private static String resolveHeader(ExcelProperty excelProperty, String fieldName) {
		String[] values = excelProperty.value();
		if (values == null || values.length == 0) {
			return fieldName;
		}
		String header = values[0];
		return header == null || header.isBlank() ? fieldName : header;
	}

	private record IndexedColumn(int index, ExportColumn column) {
	}

}
