package com.auth.service.system.file.exception;

import com.auth.common.core.model.response.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static com.auth.service.system.file.exception.FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link FileExceptionHandler} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileExceptionHandler 文件异常 HTTP 映射")
@ExtendWith(MockitoExtension.class)
class FileStorageExceptionHandlerTest {

	@Mock
	private MessageSource messageSource;

	private FileExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new FileExceptionHandler(messageSource);
	}

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	@DisplayName("FileException 应映射为对应 HTTP 状态与 Result")
	void handleFileException_mapsToResultWithI18n() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String messageKey = FILE_STORAGE_CONFIG_MISSING.getMessageKey();
		when(messageSource.getMessage(eq(messageKey), any(), eq(messageKey), eq(Locale.ENGLISH)))
			.thenReturn("Storage config missing: minio.endpoint");
		when(messageSource.getMessage(eq(messageKey), any(), eq("Storage config missing: minio.endpoint"),
				eq(Locale.ENGLISH)))
			.thenReturn("Storage config missing: minio.endpoint");

		FileStorageException exception = new FileStorageException(FILE_STORAGE_CONFIG_MISSING, "minio.endpoint");
		ResponseEntity<Result<Object>> response = handler.handleFileException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getCode()).isEqualTo(FILE_STORAGE_CONFIG_MISSING.getBizCode());
		assertThat(response.getBody().getError()).isEqualTo("FILE_STORAGE_CONFIG_MISSING");
		assertThat(response.getBody().getMessage()).isEqualTo("Storage config missing: minio.endpoint");
		assertThat(response.getBody().getExt()).containsEntry("i18nKey", messageKey);
	}

}
