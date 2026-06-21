package com.auth.service.system.admin.model.vo.me;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 个人中心用户活跃会话
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class MeUserSessionVO {

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

	@Schema(description = "是否为当前请求所在会话")
	private Boolean current;

}
