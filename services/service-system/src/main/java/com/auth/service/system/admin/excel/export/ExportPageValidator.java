package com.auth.service.system.admin.excel.export;

import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.experimental.UtilityClass;

/**
 * 管理端 Excel 导出分页参数校验
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
	 * 标准化每页条数并在越界时抛出业务异常
	 * @param pageSize 请求中的每页条数，可为 null
	 * @return 合法 pageSize
	 */
	public static int normalizePageSize(Integer pageSize) {
		int resolved = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;

		// 检查跨页行数是否超过上限
		if (resolved < ExportLimits.MIN_PAGE_SIZE || resolved > ExportLimits.MAX_PAGE_SIZE) {
			throw new SystemBusinessException(SystemAdminResultCode.EXPORT_PAGE_SIZE_INVALID, resolved);
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
			throw new SystemBusinessException(SystemAdminResultCode.EXPORT_PAGE_RANGE_INVALID, fromPage, toPage);
		}

		// 检查跨页行数是否超过上限
		long spanRows = (long) (toPage - fromPage + 1) * pageSize;
		if (spanRows > ExportLimits.MAX_EXPORT_ROWS) {
			throw new SystemBusinessException(SystemAdminResultCode.EXPORT_ROW_LIMIT_EXCEEDED,
					ExportLimits.MAX_EXPORT_ROWS, spanRows);
		}
	}

}
