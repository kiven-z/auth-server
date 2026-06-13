package com.auth.service.system.schedule.exception;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobExceptionHandler} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysJobExceptionHandler 定时任务异常 HTTP 映射")
@ExtendWith(MockitoExtension.class)
class SysJobExceptionHandlerTest {

	@Mock
	private MessageSource messageSource;

	private SysJobExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new SysJobExceptionHandler(messageSource);
	}

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	@DisplayName("SysJobException 应映射为对应 HTTP 状态与 Result")
	void handleSysJobException_mapsToResultWithI18n() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String messageKey = ScheduleResultCode.JOB_GROUP_CODE_DUPLICATE.getMessageKey();
		when(messageSource.getMessage(eq(messageKey), any(), eq(messageKey), eq(Locale.ENGLISH)))
			.thenReturn("Group code already exists");
		when(messageSource.getMessage(eq(messageKey), any(), eq("Group code already exists"), eq(Locale.ENGLISH)))
			.thenReturn("Group code already exists");

		SysJobException exception = new SysJobException(ScheduleResultCode.JOB_GROUP_CODE_DUPLICATE);
		ResponseEntity<Result<Object>> response = handler.handleSysJobException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getCode()).isEqualTo(ScheduleResultCode.JOB_GROUP_CODE_DUPLICATE.getBizCode());
		assertThat(response.getBody().getError()).isEqualTo("JOB_GROUP_CODE_DUPLICATE");
		assertThat(response.getBody().getMessage()).isEqualTo("Group code already exists");
		assertThat(response.getBody().getExt()).containsEntry("i18nKey", messageKey);
	}

}
