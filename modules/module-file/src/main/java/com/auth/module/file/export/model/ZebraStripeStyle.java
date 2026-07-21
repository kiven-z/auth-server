package com.auth.module.file.export.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 斑马线样式配置
 *
 * @author Bunny
 */
@Schema(title = "斑马线样式配置")
@Getter
@Setter
@Builder
public class ZebraStripeStyle implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "表头颜色 - RGB格式", example = "166,202,240")
	@Builder.Default
	private byte[] headerColor = new byte[] { (byte) 166, (byte) 202, (byte) 240 };

	@Schema(title = "奇数列颜色 - RGB格式", example = "255,250,240")
	@Builder.Default
	private byte[] oddColumnColor = new byte[] { (byte) 255, (byte) 250, (byte) 240 };

	@Schema(title = "偶数列颜色 - RGB格式", example = "240,248,255")
	@Builder.Default
	private byte[] evenColumnColor = new byte[] { (byte) 240, (byte) 248, (byte) 255 };

	@Schema(title = "特殊列索引", example = "6")
	@Builder.Default
	private int specialColumnIndex = 6;

	@Schema(title = "奇数行偶数列颜色 - RGB格式", example = "231,244,255")
	@Builder.Default
	private byte[] lightBlueColor = new byte[] { (byte) 231, (byte) 244, (byte) 255 };

	@Schema(title = "偶数行奇数列颜色 - RGB格式", example = "247,249,253")
	@Builder.Default
	private byte[] lightGreyColor = new byte[] { (byte) 247, (byte) 249, (byte) 253 };

	@Schema(title = "字体名称", example = "宋体")
	@Builder.Default
	private String fontName = "宋体";

	@Schema(title = "表头字体大小", example = "12")
	@Builder.Default
	private short headerFontSize = 12;

	@Schema(title = "数据行字体大小", example = "11")
	@Builder.Default
	private short dataFontSize = 11;

	@Schema(title = "边框颜色索引", example = "64")
	@Builder.Default
	private short borderColorIndex = IndexedColors.GREY_50_PERCENT.getIndex();

	@Schema(title = "特殊列颜色", example = "255,255,255")
	@Builder.Default
	private byte[] specialColumnColor = new byte[] { (byte) 255, (byte) 255, (byte) 255 };

}