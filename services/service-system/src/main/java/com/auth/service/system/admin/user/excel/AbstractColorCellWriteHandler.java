package com.auth.service.system.admin.user.excel;

import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * 抽象颜色单元格处理器
 *
 * @author Bunny
 */
public abstract class AbstractColorCellWriteHandler implements CellWriteHandler {

	protected final int targetColumnIndex;

	protected AbstractColorCellWriteHandler(int targetColumnIndex) {
		this.targetColumnIndex = targetColumnIndex;
	}

	@Override
	public void afterCellDispose(CellWriteHandlerContext context) {
		// 跳过表头
		if (BooleanUtils.isTrue(context.getHead())) {
			return;
		}

		// 只处理目标列
		if (context.getColumnIndex() != targetColumnIndex) {
			return;
		}

		// 获取单元格数据
		WriteCellData<?> cellData = context.getFirstCellData();
		if (cellData == null) {
			return;
		}

		// 获取单元格字符串的值
		String cellValue = getCellStringValue(cellData);

		WriteCellStyle writeCellStyle = cellData.getOrCreateStyle();
		WriteFont writeFont = createWriteFont(cellValue.trim());
		writeCellStyle.setWriteFont(writeFont);
	}

	/**
	 * 创建字体样式 - 由子类实现具体的颜色逻辑
	 * @param cellValue 单元格值
	 * @return WriteFont
	 */
	protected abstract WriteFont createWriteFont(String cellValue);

	/**
	 * 获取单元格的字符串值
	 */
	protected String getCellStringValue(WriteCellData<?> cellData) {
		if (cellData.getStringValue() != null) {
			return cellData.getStringValue();
		}
		if (cellData.getNumberValue() != null) {
			return cellData.getNumberValue().toString();
		}
		return "";
	}

	/**
	 * 创建基础字体样式（加粗）
	 */
	protected WriteFont createBaseWriteFont() {
		WriteFont writeFont = new WriteFont();
		writeFont.setBold(true);
		return writeFont;
	}

	/**
	 * 创建带颜色的字体样式
	 */
	protected WriteFont createColoredWriteFont(IndexedColors color) {
		WriteFont writeFont = createBaseWriteFont();
		writeFont.setColor(color.getIndex());
		return writeFont;
	}

}
