package com.auth.service.system.admin.feign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * auth 用户活跃会话远程 DTO（对齐 {@code UserSessionVO} JSON，不依赖 service-auth）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserSessionRemoteDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "用户 ID")
	private Long userId;

	@Schema(description = "会话 ID（jti）")
	private String sessionId;

	@Schema(description = "请求 IP")
	private String ipAddress;

	@Schema(description = "IP 归属地")
	private String ipRegion;

	@Schema(description = "设备类型")
	private String deviceType;

	@Schema(description = "浏览器类型")
	private String browserType;

	@Schema(description = "操作系统类型")
	private String osType;

	@Schema(description = "是否记住我")
	private Boolean rememberMe;

	@Schema(description = "Refresh Token 过期时间戳（毫秒）")
	private Long refreshTokenExpiresAt;

	@Schema(description = "登录时间戳（毫秒）")
	private Long loginAt;

}
