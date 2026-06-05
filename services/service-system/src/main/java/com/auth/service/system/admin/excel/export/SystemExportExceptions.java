package com.auth.service.system.admin.excel.export;

import com.auth.module.file.export.validation.ExportValidationCode;
import com.auth.module.file.export.validation.ExportValidationException;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.common.exception.SystemBusinessException;

import lombok.experimental.UtilityClass;

/**
 * 将 module-file 导出校验异常映射为系统管理域业务异常
 *
 * @author Bunny
 */
@UtilityClass
public class SystemExportExceptions {

	/**
	 * 翻译导出校验异常 FIXME 取消使用英文方式（module-file）
	 * @param exception module-file 校验异常
	 * @return 系统业务异常
	 */
	public static SystemBusinessException translate(ExportValidationException exception) {
		ExportValidationCode code = exception.getCode();
		Object[] args = exception.getArgs();
		return switch (code) {
			case PAGE_RANGE_INVALID ->
				new SystemBusinessException(SystemAdminResultCode.EXPORT_PAGE_RANGE_INVALID, args);
			case ROW_LIMIT_EXCEEDED ->
				new SystemBusinessException(SystemAdminResultCode.EXPORT_ROW_LIMIT_EXCEEDED, args);
			case PAGE_SIZE_INVALID -> new SystemBusinessException(SystemAdminResultCode.EXPORT_PAGE_SIZE_INVALID, args);
			case PAGE_RANGE_REQUIRED, CURRENT_PAGE_INDEX_REQUIRED, FULL_ASYNC_NOT_SUPPORTED ->
				new SystemBusinessException(SystemAdminResultCode.EXPORT_SCOPE_NOT_SUPPORTED);
		};
	}

}
