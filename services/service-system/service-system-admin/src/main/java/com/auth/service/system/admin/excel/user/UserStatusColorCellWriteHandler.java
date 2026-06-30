package com.auth.service.system.admin.excel.user;

import java.util.Map;

import com.alibaba.excel.write.metadata.style.WriteFont;
import com.auth.common.data.model.enums.UserStatus;
import com.auth.service.system.admin.excel.AbstractColorCellWriteHandler;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * 用户账号状态列着色（与 {@link UserStatus} 文案一致）。
 *
 * @author Bunny
 */
public class UserStatusColorCellWriteHandler extends AbstractColorCellWriteHandler {

	private static final Map<String, IndexedColors> COLOR_BY_DESC = Map.of(UserStatus.NORMAL.getDesc(),
			IndexedColors.GREEN, UserStatus.DISABLED.getDesc(), IndexedColors.RED, UserStatus.LOCKED.getDesc(),
			IndexedColors.LIGHT_ORANGE);

	public UserStatusColorCellWriteHandler(int statusColumnIndex) {
		super(statusColumnIndex);
	}

	/**
	 * 按用户状态文案着色。
	 * @param statusValue 单元格值
	 * @return 字体样式
	 */
	@Override
	public WriteFont createWriteFont(String statusValue) {
		IndexedColors color = COLOR_BY_DESC.getOrDefault(statusValue, IndexedColors.GREY_50_PERCENT);
		return createColoredWriteFont(color);
	}

}
