package com.auth.service.system.schedule.task.builtin.feign.support;

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
	@DisplayName("buildServiceUrl：path 已带 / 时应原样拼接")
	void buildServiceUrl_whenPathStartsWithSlash_buildsExpectedUrl() {
		assertThat(FeignInvokeSupport.buildServiceUrl("order-service", "/api/internal/sync"))
			.isEqualTo("http://order-service/api/internal/sync");
	}

	@Test
	@DisplayName("buildServiceUrl：path 未带 / 时应自动补全")
	void buildServiceUrl_whenPathMissingLeadingSlash_prependsSlash() {
		assertThat(FeignInvokeSupport.buildServiceUrl("order-service", "api/foo"))
			.isEqualTo("http://order-service/api/foo");
	}

}
