package com.auth.service.system.common.exception;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.common.exception.code.common.SystemCommonResultCode;
import com.auth.service.system.common.exception.code.message.SystemEmailResultCode;
import com.auth.service.system.common.exception.code.schedule.SystemScheduleResultCode;

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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link SystemResultResponseFactory} ????
 */
@DisplayName("SystemResultResponseFactory")
@ExtendWith(MockitoExtension.class)
class SystemResultResponseFactoryTest {

	@Mock
	private MessageSource messageSource;

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	@DisplayName("build(??)????????message ? ext.i18nKey / i18nArgs ??")
	void build_fromException_setsCodeErrorMessageAndExt() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = SystemCommonResultCode.DATA_NOT_EXIST.getMessageKey();
		when(messageSource.getMessage(eq(key), any(), eq(key), eq(Locale.ENGLISH))).thenReturn("resolved-en");
		when(messageSource.getMessage(eq(key), any(), eq("resolved-en"), eq(Locale.ENGLISH))).thenReturn("resolved-en");

		SystemBusinessException ex = new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		Result<Object> result = SystemResultResponseFactory.build(messageSource, ex);

		assertEquals(SystemCommonResultCode.DATA_NOT_EXIST.getBizCode(), result.getCode());
		assertEquals(SystemCommonResultCode.DATA_NOT_EXIST.getError(), result.getError());
		assertEquals("resolved-en", result.getMessage());
		Map<String, Object> ext = result.getExt();
		assertNotNull(ext);
		assertEquals(key, ext.get("i18nKey"));
		assertArrayEquals(new Object[0], (Object[]) ext.get("i18nArgs"));
	}

	@Test
	@DisplayName("build(??)??????? ext.i18nArgs ??")
	void build_fromException_withArgs_preservesI18nArgs() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = SystemEmailResultCode.EMAIL_TEMPLATE_NOT_FOUND.getMessageKey();
		Object[] args = new Object[] { "tpl-1" };
		when(messageSource.getMessage(eq(key), any(), eq(key), eq(Locale.ENGLISH))).thenReturn("tpl-missing");
		when(messageSource.getMessage(eq(key), any(), eq("tpl-missing"), eq(Locale.ENGLISH))).thenReturn("tpl-missing");

		SystemBusinessException ex = new SystemBusinessException(SystemEmailResultCode.EMAIL_TEMPLATE_NOT_FOUND,
				"tpl-1");
		Result<Object> result = SystemResultResponseFactory.build(messageSource, ex);

		assertEquals(SystemEmailResultCode.EMAIL_TEMPLATE_NOT_FOUND.getBizCode(), result.getCode());
		Map<String, Object> ext = result.getExt();
		assertNotNull(ext);
		assertArrayEquals(args, (Object[]) ext.get("i18nArgs"));
	}

	@Test
	@DisplayName("build(??, args)?????????????")
	void build_fromEnum_matchesFactoryContract() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = SystemScheduleResultCode.JOB_GROUP_HAS_ACTIVE_JOBS.getMessageKey();
		when(messageSource.getMessage(eq(key), any(), eq(key), eq(Locale.ENGLISH))).thenReturn("has-jobs");
		when(messageSource.getMessage(eq(key), any(), eq("has-jobs"), eq(Locale.ENGLISH))).thenReturn("has-jobs");

		Result<Object> result = SystemResultResponseFactory.build(messageSource,
				SystemScheduleResultCode.JOB_GROUP_HAS_ACTIVE_JOBS, 3);

		assertEquals(SystemScheduleResultCode.JOB_GROUP_HAS_ACTIVE_JOBS.getBizCode(), result.getCode());
		assertEquals("has-jobs", result.getMessage());
		assertEquals(key, result.getExt().get("i18nKey"));
	}

	@Test
	@DisplayName("resolveHttpStatus?404 ????? -> 404")
	void resolveHttpStatus_dataNotExist() {
		assertEquals(HttpStatus.NOT_FOUND,
				SystemResultResponseFactory.resolveHttpStatus(SystemCommonResultCode.DATA_NOT_EXIST));
	}

	@Test
	@DisplayName("resolveHttpStatus????? -> 422")
	void resolveHttpStatus_illegalRequest() {
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
				SystemResultResponseFactory.resolveHttpStatus(SystemCommonResultCode.ILLEGAL_REQUEST));
	}

	@Test
	@DisplayName("resolveHttpStatus?????? -> 503")
	void resolveHttpStatus_serviceUnavailable() {
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
				SystemResultResponseFactory.resolveHttpStatus(SystemCommonResultCode.SERVICE_UNAVAILABLE));
	}

}
