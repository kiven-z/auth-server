package com.auth.service.auth.support;

import com.auth.common.core.model.response.Result;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthBusinessExceptionResultBuilder;
import com.auth.service.auth.exception.AuthResultCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link AuthBusinessExceptionResultBuilder} 单元测试
 */
@DisplayName("AuthResultResponseFactory")
@ExtendWith(MockitoExtension.class)
class AuthBusinessExceptionResultBuilderTest {

	@Mock
	private MessageSource messageSource;

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	@DisplayName("build：稳定子码与 Result.error 一致，message 由 MessageSource 解析")
	void build_setsCodeErrorMessageAndExt() {
		// 模拟两次 getMessage：先英文兜底再当前 Locale
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = AuthResultCode.USERNAME_OR_PASSWORD_ERROR.getMessageKey();
		when(messageSource.getMessage(eq(key), any(), eq(key), eq(Locale.ENGLISH))).thenReturn("resolved-en");
		when(messageSource.getMessage(eq(key), any(), eq("resolved-en"), eq(Locale.ENGLISH))).thenReturn("resolved-en");

		AuthBusinessException ex = new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR);
		Result<Object> result = AuthBusinessExceptionResultBuilder.build(messageSource, ex);

		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR.getCode(), result.getCode());
		assertEquals(AuthResultCode.USERNAME_OR_PASSWORD_ERROR.getError(), result.getError());
		assertEquals("resolved-en", result.getMessage());
		Map<String, Object> ext = result.getExt();
		assertNotNull(ext);
		assertEquals(key, ext.get("i18nKey"));
	}

	@Test
	@DisplayName("resolveHttpStatus：用户名密码错误 -> 401")
	void resolveHttpStatus_usernamePassword_unauthorized() {
		assertEquals(HttpStatus.UNAUTHORIZED,
				AuthBusinessExceptionResultBuilder.resolveHttpStatus(AuthResultCode.USERNAME_OR_PASSWORD_ERROR));
	}

	@Test
	@DisplayName("resolveHttpStatus：限流 -> 429")
	void resolveHttpStatus_rateLimit() {
		assertEquals(HttpStatus.TOO_MANY_REQUESTS,
				AuthBusinessExceptionResultBuilder.resolveHttpStatus(AuthResultCode.TOO_MANY_REQUESTS));
	}

	@Test
	@DisplayName("resolveHttpStatus：服务器内部错误 -> 500")
	void resolveHttpStatus_serverError() {
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
				AuthBusinessExceptionResultBuilder.resolveHttpStatus(AuthResultCode.SERVER_ERROR));
	}

	@Test
	@DisplayName("resolveHttpStatus：账号锁定 -> 403")
	void resolveHttpStatus_userLocked_forbidden() {
		assertEquals(HttpStatus.FORBIDDEN,
				AuthBusinessExceptionResultBuilder.resolveHttpStatus(AuthResultCode.USER_LOCKED));
	}

	@Test
	@DisplayName("resolveHttpStatus：JSON 解析失败 -> 400")
	void resolveHttpStatus_jsonParser_badRequest() {
		assertEquals(HttpStatus.BAD_REQUEST,
				AuthBusinessExceptionResultBuilder.resolveHttpStatus(AuthResultCode.JSON_PARSER_EXCEPTION));
	}

}
