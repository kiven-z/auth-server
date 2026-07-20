package com.auth.common.web.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * WebDoc / SpringDoc configuration properties.
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.common.webdoc")
public class WebDocProperties {

	/**
	 * Toggle for webdoc auto configuration.
	 */
	private Boolean enabled = Boolean.TRUE;

	private String title = "Bunny-Admin";

	private String description = "BunnyRBAC backend management system";

	private String summary = "Bunny RBAC backend management system";

	private String version = "v1.0.0";

	private String contactName = "Bunny";

	private String contactEmail = "1319900154@qq.com";

	private String licenseName = "MIT";

	private String licenseUrl = "https://mit-license.org";

	/**
	 * OpenAPI groups.
	 */
	private List<ApiGroup> groups = defaultGroups();

	public static List<ApiGroup> defaultGroups() {
		ApiGroup web = new ApiGroup();
		web.setGroup("web");
		web.setDisplayName("默认分组");
		web.setPathsToMatch(List.of("/api/**"));

		ApiGroup inner = new ApiGroup();
		inner.setGroup("inner");
		inner.setDisplayName("内部分组");
		inner.setPathsToMatch(List.of("/api/**/inner/**"));

		return List.of(web, inner);
	}

	@Getter
	@Setter
	public static class ApiGroup {

		private String group;

		private String displayName;

		private List<String> pathsToMatch = new ArrayList<>();

	}

}
