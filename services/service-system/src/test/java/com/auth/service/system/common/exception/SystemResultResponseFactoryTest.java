package com.auth.service.system.common.exception;

import com.auth.common.core.model.response.Result;
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
 * {@link SystemResultResponseFactory} 单元测试
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
	@DisplayName("build(异常)：业务码、子码、message 与 ext.i18nKey / i18nArgs 一致")
	void build_fromException_setsCodeErrorMessageAndExt() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = SystemResultCodeEnum.DATA_NOT_EXIST.getMessageKey();
		when(messageSource.getMessage(eq(key), any(), eq(key), eq(Locale.ENGLISH))).thenReturn("resolved-en");
		when(messageSource.getMessage(eq(key), any(), eq("resolved-en"), eq(Locale.ENGLISH))).thenReturn("resolved-en");

		SystemBusinessException ex = new SystemBusinessException(SystemResultCodeEnum.DATA_NOT_EXIST);
		Result<Object> result = SystemResultResponseFactory.build(messageSource, ex);

		assertEquals(SystemResultCodeEnum.DATA_NOT_EXIST.getBizCode(), result.getCode());
		assertEquals(SystemResultCodeEnum.DATA_NOT_EXIST.getError(), result.getError());
		assertEquals("resolved-en", result.getMessage());
		Map<String, Object> ext = result.getExt();
		assertNotNull(ext);
		assertEquals(key, ext.get("i18nKey"));
		assertArrayEquals(new Object[0], (Object[]) ext.get("i18nArgs"));
	}

	@Test
	@DisplayName("build(异常)：带占位参数时 ext.i18nArgs 透传")
	void build_fromException_withArgs_preservesI18nArgs() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = SystemResultCodeEnum.EMAIL_TEMPLATE_NOT_FOUND.getMessageKey();
		Object[] args = new Object[] { "tpl-1" };
		when(messageSource.getMessage(eq(key), any(), eq(key), eq(Locale.ENGLISH))).thenReturn("tpl-missing");
		when(messageSource.getMessage(eq(key), any(), eq("tpl-missing"), eq(Locale.ENGLISH))).thenReturn("tpl-missing");

		SystemBusinessException ex = new SystemBusinessException(SystemResultCodeEnum.EMAIL_TEMPLATE_NOT_FOUND,
				"tpl-1");
		Result<Object> result = SystemResultResponseFactory.build(messageSource, ex);

		assertEquals(SystemResultCodeEnum.EMAIL_TEMPLATE_NOT_FOUND.getBizCode(), result.getCode());
		Map<String, Object> ext = result.getExt();
		assertNotNull(ext);
		assertArrayEquals(args, (Object[]) ext.get("i18nArgs"));
	}

	@Test
	@DisplayName("build(枚举, args)：与异常路径等价的关键字段")
	void build_fromEnum_matchesFactoryContract() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = SystemResultCodeEnum.JOB_GROUP_HAS_ACTIVE_JOBS.getMessageKey();
		when(messageSource.getMessage(eq(key), any(), eq(key), eq(Locale.ENGLISH))).thenReturn("has-jobs");
		when(messageSource.getMessage(eq(key), any(), eq("has-jobs"), eq(Locale.ENGLISH))).thenReturn("has-jobs");

		Result<Object> result = SystemResultResponseFactory.build(messageSource,
				SystemResultCodeEnum.JOB_GROUP_HAS_ACTIVE_JOBS, 3);

		assertEquals(SystemResultCodeEnum.JOB_GROUP_HAS_ACTIVE_JOBS.getBizCode(), result.getCode());
		assertEquals("has-jobs", result.getMessage());
		assertEquals(key, result.getExt().get("i18nKey"));
	}

	@Test
	@DisplayName("resolveHttpStatus：404 数据不存在 -> 404")
	void resolveHttpStatus_dataNotExist() {
		assertEquals(HttpStatus.NOT_FOUND,
				SystemResultResponseFactory.resolveHttpStatus(SystemResultCodeEnum.DATA_NOT_EXIST));
	}

	@Test
	@DisplayName("resolveHttpStatus：非法请求 -> 422")
	void resolveHttpStatus_illegalRequest() {
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
				SystemResultResponseFactory.resolveHttpStatus(SystemResultCodeEnum.ILLEGAL_REQUEST));
	}

	@Test
	@DisplayName("resolveHttpStatus：服务不可用 -> 503")
	void resolveHttpStatus_serviceUnavailable() {
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
				SystemResultResponseFactory.resolveHttpStatus(SystemResultCodeEnum.SERVICE_UNAVAILABLE));
	}

}
