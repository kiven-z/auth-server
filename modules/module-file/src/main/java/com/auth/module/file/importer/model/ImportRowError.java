package com.auth.module.file.importer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 单行导入失败信息。
 *
 * @author Bunny
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@Value
@Builder
@EqualsAndHashCode
@Accessors(fluent = true)
public class ImportRowError {

	/**
	 * 行号（1-based，不含表头；行数超限为 0）
	 */
	int rowIndex;

	/**
	 * 失败原因（供前端直接展示，保持历史格式）
	 */
	String message;

	/**
	 * 错误类型；仅 message 构造时为 null
	 */
	ImportErrorCode code;

	/**
	 * 错误参数；仅 message 构造时为 null
	 */
	List<Object> args;

}
