package com.auth.service.system.common.exception;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
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

import static org.junit.jupiter.api.Assertions.*;
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
	@DisplayName("build(异常)：应填充 code、error、message 与 ext.i18nKey / i18nArgs")
	void build_fromException_setsCodeErrorMessageAndExt() {
		// 验证无占位参数时 i18nArgs 为空数组
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
	@DisplayName("build(异常)：带参数时应保留 ext.i18nArgs")
	void build_fromException_withArgs_preservesI18nArgs() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = SystemCommonResultCode.DATA_IN_USE.getMessageKey();
		Object[] args = new Object[] { "sys_role" };
		when(messageSource.getMessage(eq(key), any(), eq(key), eq(Locale.ENGLISH))).thenReturn("in-use");
		when(messageSource.getMessage(eq(key), any(), eq("in-use"), eq(Locale.ENGLISH))).thenReturn("in-use");

		SystemBusinessException ex = new SystemBusinessException(SystemCommonResultCode.DATA_IN_USE, "sys_role");
		Result<Object> result = SystemResultResponseFactory.build(messageSource, ex);

		assertEquals(SystemCommonResultCode.DATA_IN_USE.getBizCode(), result.getCode());
		Map<String, Object> ext = result.getExt();
		assertNotNull(ext);
		assertArrayEquals(args, (Object[]) ext.get("i18nArgs"));
	}

	@Test
	@DisplayName("build(枚举, args)：应与工厂契约一致")
	void build_fromEnum_matchesFactoryContract() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		String key = SystemCommonResultCode.PARAM_REQUIRED.getMessageKey();
		when(messageSource.getMessage(eq(key), any(), eq(key), eq(Locale.ENGLISH))).thenReturn("param-required");
		when(messageSource.getMessage(eq(key), any(), eq("param-required"), eq(Locale.ENGLISH)))
			.thenReturn("param-required");

		Result<Object> result = SystemResultResponseFactory.build(messageSource, SystemCommonResultCode.PARAM_REQUIRED,
				"Recipients");

		assertEquals(SystemCommonResultCode.PARAM_REQUIRED.getBizCode(), result.getCode());
		assertEquals("param-required", result.getMessage());
		assertEquals(key, result.getExt().get("i18nKey"));
	}

	@Test
	@DisplayName("resolveHttpStatus：404 数据不存在 -> 404")
	void resolveHttpStatus_dataNotExist() {
		assertEquals(HttpStatus.NOT_FOUND,
				SystemResultResponseFactory.resolveHttpStatus(SystemCommonResultCode.DATA_NOT_EXIST));
	}

	@Test
	@DisplayName("resolveHttpStatus：必填参数为空 -> 422")
	void resolveHttpStatus_paramRequired() {
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
				SystemResultResponseFactory.resolveHttpStatus(SystemCommonResultCode.PARAM_REQUIRED));
	}

	@Test
	@DisplayName("resolveHttpStatus：服务不可用 -> 503")
	void resolveHttpStatus_serviceUnavailable() {
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
				SystemResultResponseFactory.resolveHttpStatus(SystemCommonResultCode.SERVICE_UNAVAILABLE));
	}

}
