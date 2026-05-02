package com.auth.service.system.admin.user.excel;

import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * 权限类型颜色处理器
 *
 * @author Bunny
 */
public class PermissionTypeColorCellWriteHandler extends AbstractColorCellWriteHandler {

    public PermissionTypeColorCellWriteHandler(int permissionTypeColumnIndex) {
        super(permissionTypeColumnIndex);
    }

    @Override
    public WriteFont createWriteFont(String permissionTypeValue) {
        return switch (permissionTypeValue) {
            case "API权限" -> createColoredWriteFont(IndexedColors.SKY_BLUE);
            case "按钮权限" -> createColoredWriteFont(IndexedColors.SEA_GREEN);
            default -> createColoredWriteFont(IndexedColors.GREY_25_PERCENT);
        };
    }

}
