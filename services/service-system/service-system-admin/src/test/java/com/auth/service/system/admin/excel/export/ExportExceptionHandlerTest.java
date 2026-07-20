package com.auth.service.system.admin.excel.export;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.exception.ExportException;
import com.auth.service.system.admin.exception.ExportExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ExportExceptionHandler} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ExportExceptionHandler 导出异常 HTTP 映射")
class ExportExceptionHandlerTest {

	private ExportExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new ExportExceptionHandler();
	}

	@Test
	@DisplayName("ExportException 返回 400 与英文 message，无 i18n ext")
	void handleExportException_returnsBadRequestWithEnglishMessage() {
		ExportException exception = ExportException.pageRangeInvalid(2, 1);

		ResponseEntity<Result<Object>> response = handler.handleExportException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getCode()).isEqualTo(400);
		assertThat(response.getBody().getError()).isEqualTo("EXPORT_VALIDATION_FAILED");
		assertThat(response.getBody().getMessage()).isEqualTo("Invalid export page range: fromPage=2, toPage=1");
		assertThat(response.getBody().getExt()).isNull();
	}

}
