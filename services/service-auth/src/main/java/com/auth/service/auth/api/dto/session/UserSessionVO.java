package com.auth.service.auth.api.dto.session;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 管理端用户活跃会话 VO（对齐前端 session.ts，不含 refreshTokenHash）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class UserSessionVO {

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
