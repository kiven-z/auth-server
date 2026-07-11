package com.auth.service.auth.model.response;

import com.auth.common.core.annotation.JsonStringFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 刷新令牌响应（含当前授权快照，供前端同步按钮/角色态）
 *
 * @author Bunny
 */
@Getter
@Setter
public class RefreshTokenResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(description = "用户ID")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "角色")
	private List<String> roles;

	@Schema(description = "权限")
	private List<String> permissions;

	@Schema(description = "访问令牌")
	private String accessToken;

	@JsonIgnore
	@Schema(description = "刷新令牌（仅服务端 Cookie 下发使用，不对前端 JSON 暴露）")
	private String refreshToken;

	@Schema(description = "阅读过期天数")
	private Long readMeDay;

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	@Schema(description = "过期时间")
	private LocalDateTime expires;

}
