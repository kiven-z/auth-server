package com.auth.service.system.admin.excel;

import com.alibaba.excel.write.metadata.style.WriteFont;

/**
 * 状态颜色处理器
 *
 * @author Bunny
 */
public class StatusColorCellWriteHandler extends AbstractColorCellWriteHandler {

	public StatusColorCellWriteHandler(int statusColumnIndex) {
		super(statusColumnIndex);
	}

	@Override
	public WriteFont createWriteFont(String statusValue) {
		return createColoredWriteFont(EnableStatusLabels.colorForLabel(statusValue));
	}

}
