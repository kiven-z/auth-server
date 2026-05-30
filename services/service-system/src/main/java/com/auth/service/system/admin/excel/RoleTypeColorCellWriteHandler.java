package com.auth.service.system.admin.excel;

import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * 角色类型颜色单元格处理器
 *
 * @author Bunny
 */
public class RoleTypeColorCellWriteHandler extends AbstractColorCellWriteHandler {

	public RoleTypeColorCellWriteHandler(int roleTypeColumnIndex) {
		super(roleTypeColumnIndex);
	}

	@Override
	public WriteFont createWriteFont(String roleTypeValue) {
		return switch (roleTypeValue) {
			case "业务角色" -> createColoredWriteFont(IndexedColors.SKY_BLUE);
			case "系统角色" -> createColoredWriteFont(IndexedColors.CORAL);
			case "自定义角色" -> createColoredWriteFont(IndexedColors.SEA_GREEN);
			default -> createColoredWriteFont(IndexedColors.GREY_25_PERCENT);
		};
	}

}
