package com.auth.gateway.exception;

import com.auth.common.core.i18n.I18nMessageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link GatewayBusinessException} 与 {@link I18nMessageProvider} 契约对齐测试
 */
@DisplayName("GatewayBusinessException — I18nMessageProvider")
class GatewayBusinessExceptionI18nTest {

	@Test
	@DisplayName("实现 I18nMessageProvider，messageKey 与枚举 i18nKey 一致")
	void implementsProvider_messageKeyMatchesEnum() {
		GatewayBusinessException ex = new GatewayBusinessException(GatewayResultCodeEnum.ROUTE_NOT_FOUND);

		assertInstanceOf(I18nMessageProvider.class, ex);
		assertEquals(GatewayResultCodeEnum.ROUTE_NOT_FOUND.getI18nKey(), ex.getMessageKey());
		assertEquals(ex.getMessageKey(), ex.getI18nKey());
		assertArrayEquals(new Object[0], ex.getMessageArgs());
	}

	@Test
	@DisplayName("getMessageArgs 与构造参数为同一引用（只读消费）")
	void messageArgsSameReferenceAsConstructed() {
		Object[] args = { "a", 1 };
		GatewayBusinessException ex = new GatewayBusinessException(GatewayResultCodeEnum.INTERNAL_ERROR, args);

		assertSame(args, ex.getMessageArgs());
	}

}
