package com.auth.service.system.schedule.support.builtin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BuiltinHttpSupport} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("BuiltinHttpSupport HTTP 与微服务 URL 组装")
class BuiltinHttpSupportTest {

	@Test
	@DisplayName("buildServiceUrl：path 无斜杠时应自动补全")
	void buildServiceUrl_whenPathMissingLeadingSlash_normalizes() {
		assertThat(BuiltinHttpSupport.buildServiceUrl("order-service", "api/orders"))
			.isEqualTo("http://order-service/api/orders");
	}

	@Test
	@DisplayName("buildServiceUrl：path 已有斜杠时应保持")
	void buildServiceUrl_whenPathHasLeadingSlash_keepsPath() {
		assertThat(BuiltinHttpSupport.buildServiceUrl("order-service", "/api/orders"))
			.isEqualTo("http://order-service/api/orders");
	}

}
