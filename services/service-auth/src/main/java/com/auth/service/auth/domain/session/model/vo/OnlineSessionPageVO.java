package com.auth.service.auth.domain.session.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局在线会话分页行
 *
 * @author Bunny
 */
@Schema(name = "OnlineSessionPageVO", title = "全局在线会话分页行")
@Getter
@Setter
@ToString
public class OnlineSessionPageVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "用户 ID")
	private Long userId;

	@Schema(title = "用户名")
	private String username;

	@Schema(title = "昵称")
	private String nickname;

	@Schema(title = "会话 ID（jti）")
	private String sessionId;

	@Schema(title = "IP 地址")
	private String ipAddress;

	@Schema(title = "IP 归属地")
	private String ipRegion;

	@Schema(title = "设备类型")
	private String deviceType;

	@Schema(title = "浏览器")
	private String browserType;

	@Schema(title = "操作系统")
	private String osType;

	@Schema(title = "登录时间戳（毫秒）")
	private Long loginAt;

	@Schema(title = "是否记住我")
	private Boolean rememberMe;

}
