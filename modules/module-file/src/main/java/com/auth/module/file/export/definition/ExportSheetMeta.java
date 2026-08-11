package com.auth.module.file.export.definition;

import com.alibaba.excel.write.handler.CellWriteHandler;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 导出工作表元信息
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class ExportSheetMeta {

	/**
	 * 工作表名称
	 */
	String sheetName;

	/**
	 * 同步导出下载文件名前缀（不含扩展名）
	 */
	String downloadFilePrefix;

	/**
	 * 异步导出任务文件名前缀（不含扩展名）
	 */
	String taskFilePrefix;

	/**
	 * Map 导出行列定义（必填）
	 */
	@Builder.Default
	List<ExportColumn> columns = List.of();

	/**
	 * EasyExcel 写入处理器（为空时 Writer 默认 BLUE_GREY_TECH）
	 */
	@Builder.Default
	List<CellWriteHandler> writeHandlers = List.of();

}
