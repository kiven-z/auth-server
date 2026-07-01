package com.auth.common.web.autoconfigure;

import java.util.List;

import com.auth.common.web.config.OpenApiDocConfig;
import com.auth.common.web.config.properties.WebDocProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CommonWebDocAutoConfigurationTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(CommonWebDocAutoConfiguration.class));

	@Test
	void testAutoConfiguration_WithDefaultSettings() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(WebDocProperties.class);
			assertThat(context).hasSingleBean(OpenApiDocConfig.class);
			assertThat(context).hasSingleBean(OpenAPI.class);
			assertThat(context).getBean("groupedOpenApis").isInstanceOf(List.class);
		});
	}

	@Test
	void testAutoConfiguration_WhenDisabled() {
		contextRunner.withPropertyValues("auth.common.webdoc.enabled=false").run(context -> {
			assertThat(context).doesNotHaveBean(OpenApiDocConfig.class);
			assertThat(context).doesNotHaveBean(OpenAPI.class);
		});
	}

	@Test
	void testAutoConfiguration_WithCustomTitle() {
		contextRunner.withPropertyValues("auth.common.webdoc.title=My Custom API").run(context -> {
			assertThat(context).hasSingleBean(OpenAPI.class);
			OpenAPI openAPI = context.getBean(OpenAPI.class);
			assertThat(openAPI.getInfo().getTitle()).contains("My Custom API");
		});
	}

	@Test
	@DisplayName("defaultOpenApi 注册 bearer JWT 安全方案并全局启用")
	void testDefaultOpenApi_IncludesBearerSecurityScheme() {
		contextRunner.run(context -> {
			// 验证 Swagger UI Authorize 所需的 HTTP Bearer JWT 方案
			OpenAPI openAPI = context.getBean(OpenAPI.class);
			assertThat(openAPI.getComponents()).isNotNull();
			assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");

			SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
			assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
			assertThat(scheme.getScheme()).isEqualTo("bearer");
			assertThat(scheme.getBearerFormat()).isEqualTo("JWT");

			assertThat(openAPI.getSecurity()).isNotEmpty();
			assertThat(openAPI.getSecurity().get(0)).containsKey("bearerAuth");
		});
	}

	@Test
	void testDefaultGroups_AreLoaded() {
		contextRunner.run(context -> {
			WebDocProperties properties = context.getBean(WebDocProperties.class);
			assertThat(properties.getGroups()).hasSize(2);
			assertThat(properties.getGroups().get(0).getGroup()).isEqualTo("web");
			assertThat(properties.getGroups().get(1).getGroup()).isEqualTo("inner");
		});
	}

}
