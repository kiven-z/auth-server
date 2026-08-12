package com.auth.module.file.export.writer;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.auth.module.file.export.model.enums.ZebraStripeStyleType;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 临时 Excel 文件写入共用骨架
 *
 * @author Bunny
 */
@UtilityClass
public class TempExcelFileSupport {

	private static final String XLSX_SUFFIX = ".xlsx";

	/**
	 * 创建临时 xlsx 并写入；失败时删除临时文件
	 * @param action 写入动作
	 * @return 临时文件路径
	 * @throws IOException 写入失败
	 */
	public static Path writeToTempFile(TempFileSupport.OutputStreamAction action) throws IOException {
		return TempFileSupport.writeToTempFile(XLSX_SUFFIX, action);
	}

	/**
	 * 注册默认列宽、斑马样式与业务处理器（业务处理器为空时默认 BLUE_GREY_TECH）
	 * @param builder EasyExcel builder
	 * @param writeHandlers 业务处理器（可空）
	 */
	public static void applyDefaultHandlers(ExcelWriterBuilder builder, List<CellWriteHandler> writeHandlers) {
		builder.registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());
		if (writeHandlers == null || writeHandlers.isEmpty()) {
			CellWriteHandler cellWriteHandler = ZebraStripeStyleType.getHandler(ZebraStripeStyleType.BLUE_GREY_TECH);
			builder.registerWriteHandler(cellWriteHandler);
			return;
		}
		for (CellWriteHandler handler : writeHandlers) {
			builder.registerWriteHandler(handler);
		}
	}

}
