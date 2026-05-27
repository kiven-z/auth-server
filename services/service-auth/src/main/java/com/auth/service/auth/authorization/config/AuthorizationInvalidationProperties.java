package com.auth.service.auth.authorization.config;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 授权失效编排可调参数
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.auth.invalidation")
@Configuration
@Validated
public class AuthorizationInvalidationProperties {

	/**
	 * 递增与画像刷新/驱逐的每批大小
	 */
	@NotNull
	private Integer batchSize = 500;

	/**
	 * 是否启用策略码
	 */
	private boolean policyRoleBridgeEnabled = false;

}
