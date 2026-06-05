package com.auth.module.file.export.validation;

import com.auth.module.file.export.model.ExportLimits;
import lombok.experimental.UtilityClass;

/**
 * 同步导出分页参数校验
 *
 * @author Bunny
 */
@UtilityClass
public class ExportPageValidator {

	/**
	 * 未传 pageSize 时的默认每页条数
	 */
	public static final int DEFAULT_PAGE_SIZE = 30;

	/**
	 * 标准化每页条数并在越界时抛出校验异常
	 * @param pageSize 请求中的每页条数，可为 null
	 * @return 合法 pageSize
	 */
	public static int normalizePageSize(Integer pageSize) {
		int resolved = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
		if (resolved < ExportLimits.MIN_PAGE_SIZE || resolved > ExportLimits.MAX_PAGE_SIZE) {
			throw new ExportValidationException(ExportValidationCode.PAGE_SIZE_INVALID, resolved);
		}
		return resolved;
	}

	/**
	 * 校验页码区间与跨页累计行数上限
	 * @param fromPage 起始页（1-based，含）
	 * @param toPage 结束页（1-based，含）
	 * @param pageSize 每页条数
	 */
	public static void validatePageRange(int fromPage, int toPage, int pageSize) {
		if (toPage < fromPage) {
			throw new ExportValidationException(ExportValidationCode.PAGE_RANGE_INVALID, fromPage, toPage);
		}

		long spanRows = (long) (toPage - fromPage + 1) * pageSize;
		if (spanRows > ExportLimits.MAX_EXPORT_ROWS) {
			throw new ExportValidationException(ExportValidationCode.ROW_LIMIT_EXCEEDED, ExportLimits.MAX_EXPORT_ROWS,
					spanRows);
		}
	}

}
