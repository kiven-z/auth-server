package com.auth.service.system.common.exception;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.common.exception.code.SystemEmailResultCode;
import com.auth.service.system.common.exception.code.SystemMessageResultCode;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link SystemBusinessExceptionHandler} 对 {@link SystemMessageException} 的处理单元测试
 */
@DisplayName("SystemBusinessExceptionHandler 消息异常")
@ExtendWith(MockitoExtension.class)
class SystemBusinessExceptionHandlerMessageTest {

	@Mock
	private MessageSource messageSource;

	private SystemBusinessExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new SystemBusinessExceptionHandler(messageSource);
	}

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	@DisplayName("EMAIL_SEND_TO_EMPTY 返回 400 与 i18n ext")
	void handleSystemMessage_emailValidation() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = SystemEmailResultCode.EMAIL_SEND_TO_EMPTY.getMessageKey();
		when(messageSource.getMessage(eq(key), any(), eq(key), eq(Locale.ENGLISH)))
			.thenReturn("Recipients cannot be empty");
		when(messageSource.getMessage(eq(key), any(), eq("Recipients cannot be empty"), eq(Locale.ENGLISH)))
			.thenReturn("Recipients cannot be empty");

		SystemMessageException exception = new SystemMessageException(SystemEmailResultCode.EMAIL_SEND_TO_EMPTY);
		ResponseEntity<Result<Object>> response = handler.handleSystemMessage(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		Result<Object> body = response.getBody();
		assertNotNull(body);
		assertEquals(SystemEmailResultCode.EMAIL_SEND_TO_EMPTY.getBizCode(), body.getCode());
		assertEquals(key, body.getExt().get("i18nKey"));
	}

	@Test
	@DisplayName("SMS_DYPNS_SEND_FAILED 返回 502 与 i18n ext")
	void handleSystemMessage_smsSendFailed() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = SystemMessageResultCode.SMS_DYPNS_SEND_FAILED.getMessageKey();
		Object[] args = new Object[] { "vendor-error" };
		when(messageSource.getMessage(eq(key), eq(args), eq(key), eq(Locale.ENGLISH))).thenReturn("send failed");
		when(messageSource.getMessage(eq(key), eq(args), eq("send failed"), eq(Locale.ENGLISH)))
			.thenReturn("send failed");

		SystemMessageException exception = new SystemMessageException(SystemMessageResultCode.SMS_DYPNS_SEND_FAILED,
				"vendor-error");
		ResponseEntity<Result<Object>> response = handler.handleSystemMessage(exception);

		assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
		Result<Object> body = response.getBody();
		assertNotNull(body);
		assertEquals(SystemMessageResultCode.SMS_DYPNS_SEND_FAILED.getBizCode(), body.getCode());
	}

}
