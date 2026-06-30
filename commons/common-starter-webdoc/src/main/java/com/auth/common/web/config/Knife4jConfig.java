package com.auth.common.web.config;

import java.util.ArrayList;
import java.util.List;

import com.auth.common.web.config.properties.WebDocProperties;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 如果要自定义，需要自己声明一个 Knife4jConfig Bean
 *
 * @author Bunny
 */
@Slf4j
@ConditionalOnProperty(prefix = "auth.common.webdoc", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(Knife4jConfig.class)
@Configuration
public class Knife4jConfig {

	private final WebDocProperties webDocProperties;

	private final Environment environment;

	public Knife4jConfig(WebDocProperties webDocProperties, Environment environment) {
		this.webDocProperties = webDocProperties;
		this.environment = environment;
	}

	/**
	 * 配置knife4j的OpenAPI 如果要自定义，需要自己声明一个 defaultOpenAPI Bean
	 * @return OpenAPI
	 */
	@Bean
	public OpenAPI defaultOpenApi() {
		String port = environment.getProperty("server.port", "8080");
		String serviceName = environment.getProperty("spring.application.name", "service");
		String url = "http://localhost:" + port;

		// 作者等信息
		Contact contact = new Contact().name(webDocProperties.getContactName())
			.email(webDocProperties.getContactEmail())
			.url(url);
		// 使用协议
		License license = new License().name(webDocProperties.getLicenseName()).url(webDocProperties.getLicenseUrl());
		// 相关信息
		Info info = new Info().title(webDocProperties.getTitle() + "-" + serviceName)
			.contact(contact)
			.license(license)
			.description(webDocProperties.getDescription())
			.summary(webDocProperties.getSummary())
			.termsOfService(url)
			.version(webDocProperties.getVersion());

		return new OpenAPI().info(info).externalDocs(new ExternalDocumentation());
	}

	/**
	 * 配置 knife4j 分组（默认 web/inner，可被配置覆盖）
	 * @return GroupedOpenApi 列表
	 */
	@Bean
	public List<GroupedOpenApi> groupedOpenApis() {
		List<WebDocProperties.ApiGroup> configuredGroups = webDocProperties.getGroups();
		List<WebDocProperties.ApiGroup> groups = (configuredGroups == null || configuredGroups.isEmpty())
				? WebDocProperties.defaultGroups() : configuredGroups;

		List<GroupedOpenApi> apis = new ArrayList<>(groups.size());
		for (WebDocProperties.ApiGroup group : groups) {
			apis.add(GroupedOpenApi.builder()
				.displayName(group.getDisplayName())
				.group(group.getGroup())
				.pathsToMatch(group.getPathsToMatch().toArray(String[]::new))
				.build());
		}
		return apis;
	}

}
