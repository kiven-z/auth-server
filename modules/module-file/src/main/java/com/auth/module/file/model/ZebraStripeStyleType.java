package com.auth.module.file.model;

import com.alibaba.excel.write.handler.CellWriteHandler;
import com.auth.module.file.handler.ZebraStripeStyleHandler;
import lombok.Getter;

/**
 * EasyExcel 中表格样式枚举，斑马线风格枚举 在使用时，通过 ZebraStripeStyleFactoryUtil 传入枚举即可
 *
 * @author Bunny
 */
@Getter
public enum ZebraStripeStyleType {

	/**
	 * 柔和绿色风格（适合财务/环保类报表）
	 */
	GREEN_SOFT(new byte[] { (byte) 198, (byte) 224, (byte) 180 }, new byte[] { (byte) 255, (byte) 253, (byte) 235 },
			new byte[] { (byte) 240, (byte) 255, (byte) 240 }, new byte[] { (byte) 235, (byte) 245, (byte) 230 },
			new byte[] { (byte) 225, (byte) 245, (byte) 240 }),

	/**
	 * 优雅紫色风格（适合创意/设计类报表）
	 */
	PURPLE_ELEGANT(new byte[] { (byte) 200, (byte) 180, (byte) 230 }, new byte[] { (byte) 250, (byte) 240, (byte) 255 },
			new byte[] { (byte) 245, (byte) 235, (byte) 255 }, new byte[] { (byte) 245, (byte) 240, (byte) 255 },
			new byte[] { (byte) 255, (byte) 240, (byte) 245 }),

	/**
	 * 简洁蓝灰风格（适合技术/数据类报表）
	 */
	BLUE_GREY_TECH(new byte[] { (byte) 140, (byte) 160, (byte) 180 }, new byte[] { (byte) 240, (byte) 245, (byte) 250 },
			new byte[] { (byte) 250, (byte) 250, (byte) 255 }, new byte[] { (byte) 235, (byte) 240, (byte) 245 },
			new byte[] { (byte) 245, (byte) 245, (byte) 250 }),

	/**
	 * 温暖橙色风格（适合销售/活动类报表）
	 */
	ORANGE_WARM(new byte[] { (byte) 253, (byte) 185, (byte) 120 }, new byte[] { (byte) 255, (byte) 250, (byte) 220 },
			new byte[] { (byte) 255, (byte) 245, (byte) 235 }, new byte[] { (byte) 255, (byte) 243, (byte) 230 },
			new byte[] { (byte) 255, (byte) 235, (byte) 235 }),

	/**
	 * 经典灰色商务风格
	 */
	GREY_BUSINESS(new byte[] { (byte) 128, (byte) 128, (byte) 128 }, new byte[] { (byte) 250, (byte) 250, (byte) 250 },
			new byte[] { (byte) 255, (byte) 255, (byte) 255 }, new byte[] { (byte) 255, (byte) 255, (byte) 255 },
			new byte[] { (byte) 245, (byte) 245, (byte) 245 });

	private final ZebraStripeStyle style;

	ZebraStripeStyleType(byte[] headerColor, byte[] oddColumnColor, byte[] evenColumnColor, byte[] lightBlueColor,
			byte[] lightGreyColor) {
		this.style = ZebraStripeStyle.builder()
			.headerColor(headerColor)
			.oddColumnColor(oddColumnColor)
			.evenColumnColor(evenColumnColor)
			.lightBlueColor(lightBlueColor)
			.lightGreyColor(lightGreyColor)
			.build();
	}

	/**
	 * 获取斑马线样式处理器
	 * @param styleType 样式枚举
	 * @return 斑马线样式处理器
	 */
	public static CellWriteHandler getHandler(ZebraStripeStyleType styleType) {
		ZebraStripeStyle style = styleType.getStyle();
		return new ZebraStripeStyleHandler(style);
	}

}