package com.auth.gateway.config;

import com.auth.gateway.security.GatewayAuthMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关安全校验配置（仅做 JWT 基础校验，不负责签发）
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.gateway.security")
@RefreshScope
@Configuration
public class GatewaySecurityProperties {

	/**
	 * 是否启用网关 JWT 校验（开发/联调可快速关闭）
	 */
	private boolean enabled = true;

	/**
	 * 总是允许的路径（匿名）
	 */
	private List<String> permitPaths = new ArrayList<>();

	/**
	 * 内部接口路径（Ant 风格）：命中时必须携带 {@code X-Internal-JWT}，否则网关直接拒绝（默认 404） 例："/api/inner/"
	 */
	private List<String> internalPaths = new ArrayList<>();

	/**
	 * 网关鉴权模式
	 */
	private GatewayAuthMode mode = GatewayAuthMode.FAIL_ON_INVALID_TOKEN;

}
