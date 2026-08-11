package com.auth.module.file.export.definition;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;

/**
 * 导出列定义（Map 行动态表头，可 JSON 快照）
 *
 * @author Bunny
 */
@Value
@Builder
@Jacksonized
public class ExportColumn implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Map 字段键
	 */
	String fieldKey;

	/**
	 * 表头标题
	 */
	String header;

}
