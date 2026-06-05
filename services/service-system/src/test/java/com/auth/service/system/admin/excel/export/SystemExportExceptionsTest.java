package com.auth.service.system.admin.excel.export;

import com.auth.module.file.export.validation.ExportValidationCode;
import com.auth.module.file.export.validation.ExportValidationException;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.common.exception.SystemBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SystemExportExceptions} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SystemExportExceptions 导出异常映射")
class SystemExportExceptionsTest {

	@Test
	@DisplayName("页码区间无效映射为 EXPORT_PAGE_RANGE_INVALID")
	void translate_whenPageRangeInvalid_mapsResultCode() {
		ExportValidationException exception = new ExportValidationException(ExportValidationCode.PAGE_RANGE_INVALID, 2,
				1);

		SystemBusinessException translated = SystemExportExceptions.translate(exception);

		assertThat(translated.getResultCode()).isEqualTo(SystemAdminResultCode.EXPORT_PAGE_RANGE_INVALID);
		assertThat(translated.getMessageArgs()).containsExactly(2, 1);
	}

	@Test
	@DisplayName("FULL_ASYNC 映射为 EXPORT_SCOPE_NOT_SUPPORTED")
	void translate_whenFullAsyncNotSupported_mapsScopeNotSupported() {
		ExportValidationException exception = new ExportValidationException(
				ExportValidationCode.FULL_ASYNC_NOT_SUPPORTED);

		SystemBusinessException translated = SystemExportExceptions.translate(exception);

		assertThat(translated.getResultCode()).isEqualTo(SystemAdminResultCode.EXPORT_SCOPE_NOT_SUPPORTED);
	}

}
