package com.auth.service.system.admin.handler.excel;

import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * @author Bunny
 */
public class UserStatusColorCellWriteHandler extends AbstractColorCellWriteHandler {

	public UserStatusColorCellWriteHandler(int statusColumnIndex) {
		super(statusColumnIndex);
	}

	/**
	 * 创建字体样式 - 由子类实现具体的颜色逻辑
	 * @param statusValue 单元格值
	 */
	@Override
	public WriteFont createWriteFont(String statusValue) {
		return switch (statusValue) {
			case "正常" -> createColoredWriteFont(IndexedColors.GREEN);
			case "禁用" -> createColoredWriteFont(IndexedColors.RED);
			case "锁定" -> createColoredWriteFont(IndexedColors.LIGHT_ORANGE);
			default -> createColoredWriteFont(IndexedColors.GREY_50_PERCENT);
		};
	}

}
