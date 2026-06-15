package com.auth.service.system.schedule.support.builtin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FeignInvokeSupport} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FeignInvokeSupport 微服务 URL 组装")
class FeignInvokeSupportTest {

	@Test
	@DisplayName("buildServiceUrl：path 无斜杠时应自动补全")
	void buildServiceUrl_whenPathMissingLeadingSlash_normalizes() {
		assertThat(FeignInvokeSupport.buildServiceUrl("order-service", "api/orders"))
			.isEqualTo("http://order-service/api/orders");
	}

	@Test
	@DisplayName("buildServiceUrl：path 已有斜杠时应保持")
	void buildServiceUrl_whenPathHasLeadingSlash_keepsPath() {
		assertThat(FeignInvokeSupport.buildServiceUrl("order-service", "/api/orders"))
			.isEqualTo("http://order-service/api/orders");
	}

}
