package com.auth.gateway.exception;

import com.auth.common.core.i18n.I18nMessageProvider;
import com.auth.common.core.model.response.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayBusinessExceptionHandlerTest {

	@Mock
	private MessageSource messageSource;

	private GatewayBusinessExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new GatewayBusinessExceptionHandler(messageSource);
	}

	@Test
	@DisplayName("测试将结构化异常映射为结果")
	void mapsStructuredExceptionToResult() {
		GatewayBusinessException ex = new GatewayBusinessException(GatewayResultCodeEnum.ROUTE_NOT_FOUND);
		assertInstanceOf(I18nMessageProvider.class, ex);

		String key = GatewayResultCodeEnum.ROUTE_NOT_FOUND.getI18nKey();
		String defaultEn = GatewayResultCodeEnum.ROUTE_NOT_FOUND.getDefaultMessageEn();
		when(messageSource.getMessage(eq(key), any(), eq(defaultEn), eq(Locale.ENGLISH))).thenReturn("NO_ROUTE");
		when(messageSource.getMessage(eq(key), any(), eq("NO_ROUTE"), eq(Locale.ENGLISH))).thenReturn("NO_ROUTE");

		ResponseEntity<Result<Object>> response = handler.exceptionHandler(ex);
		assertEquals(404, response.getStatusCode().value());

		Result<Object> body = response.getBody();
		assertNotNull(body);
		assertEquals(GatewayResultCodeEnum.ROUTE_NOT_FOUND.fullBizCode(), body.getCode());
		assertEquals(GatewayResultCodeEnum.ROUTE_NOT_FOUND.getError(), body.getError());
		assertEquals("NO_ROUTE", body.getMessage());

		Map<String, Object> ext = body.getExt();
		assertNotNull(ext);
		assertEquals(GatewayResultCodeEnum.ROUTE_NOT_FOUND.getI18nKey(), ext.get("i18nKey"));
	}

	@Test
	@DisplayName("测试 INTERNAL_ERROR 枚举映射为 500 与对应业务码")
	void mapsInternalErrorEnumToResult() {
		GatewayBusinessException ex = new GatewayBusinessException(GatewayResultCodeEnum.INTERNAL_ERROR);

		String key = "gateway.internal.error";
		String defaultEn = GatewayResultCodeEnum.INTERNAL_ERROR.getDefaultMessageEn();
		when(messageSource.getMessage(eq(key), any(), eq(defaultEn), eq(Locale.ENGLISH))).thenReturn("INTERNAL_MSG");
		when(messageSource.getMessage(eq(key), any(), eq("INTERNAL_MSG"), eq(Locale.ENGLISH)))
			.thenReturn("INTERNAL_MSG");

		ResponseEntity<Result<Object>> response = handler.exceptionHandler(ex);
		assertEquals(500, response.getStatusCode().value());

		Result<Object> body = response.getBody();
		assertNotNull(body);
		assertEquals(GatewayResultCodeEnum.INTERNAL_ERROR.fullBizCode(), body.getCode());
		assertEquals(GatewayResultCodeEnum.INTERNAL_ERROR.getError(), body.getError());
		assertEquals("INTERNAL_MSG", body.getMessage());
	}

}
