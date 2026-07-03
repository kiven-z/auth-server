package com.auth.service.system.message.exception;

import java.util.Locale;

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

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link MessageExceptionHandler} 单元测试
 *
 * @author Bunny
 */
@DisplayName("MessageExceptionHandler 消息异常 HTTP 映射")
@ExtendWith(MockitoExtension.class)
class MessageExceptionHandlerTest {

	@Mock
	private MessageSource messageSource;

	private MessageExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new MessageExceptionHandler(messageSource);
	}

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	@DisplayName("MessageException 应映射为对应 HTTP 状态与 Result")
	void handleMessageException_mapsToResultWithI18n() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String messageKey = PARAM_REQUIRED.getMessageKey();
		when(messageSource.getMessage(eq(messageKey), any(), eq(messageKey), eq(Locale.ENGLISH)))
			.thenReturn("Parameter required: 短信区域");
		when(messageSource.getMessage(eq(messageKey), any(), eq("Parameter required: 短信区域"), eq(Locale.ENGLISH)))
			.thenReturn("Parameter required: 短信区域");

		MessageException exception = new MessageException(PARAM_REQUIRED, "短信区域");
		ResponseEntity<Result<Object>> response = handler.handleMessageException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getCode()).isEqualTo(PARAM_REQUIRED.getBizCode());
		assertThat(response.getBody().getError()).isEqualTo("PARAM_REQUIRED");
		assertThat(response.getBody().getMessage()).isEqualTo("Parameter required: 短信区域");
		assertThat(response.getBody().getExt()).containsEntry("i18nKey", messageKey);
	}

}
