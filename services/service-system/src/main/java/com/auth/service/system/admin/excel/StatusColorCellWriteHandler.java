package com.auth.service.system.admin.excel;

import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.util.List;

/**
 * 状态颜色处理器
 *
 * @author Bunny
 */
public class StatusColorCellWriteHandler extends AbstractColorCellWriteHandler {

    /**
     * 启用状态值
     */
    private static final List<String> ENABLED_STATUS_VALUES = List.of("1", "启用", "正常");

    /**
     * 禁用状态值
     */
    private static final List<String> DISABLED_STATUS_VALUES = List.of("0", "停用", "禁用");

    public StatusColorCellWriteHandler(int statusColumnIndex) {
        super(statusColumnIndex);
    }

    @Override
    public WriteFont createWriteFont(String statusValue) {
        if (ENABLED_STATUS_VALUES.contains(statusValue)) {
            return createColoredWriteFont(IndexedColors.GREEN);
        } else if (DISABLED_STATUS_VALUES.contains(statusValue)) {
            return createColoredWriteFont(IndexedColors.RED);
        } else {
            return createColoredWriteFont(IndexedColors.GREY_50_PERCENT);
        }
    }

}
