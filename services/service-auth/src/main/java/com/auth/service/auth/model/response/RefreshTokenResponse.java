package com.auth.service.auth.model.response;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 刷新令牌响应
 *
 * @author Bunny
 */
@Getter
@Setter
public class RefreshTokenResponse implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final String EXPIRES_FORMATTER = "yyyy/MM/dd HH:mm:ss";

	@Schema(description = "访问令牌")
	private String accessToken;

	@Schema(description = "刷新令牌（仅服务端 Cookie 下发使用，不对前端 JSON 暴露）")
	@JsonIgnore
	private String refreshToken;

	@Schema(description = "阅读过期天数")
	private Long readMeDay;

	@Schema(description = "过期时间")
	@JsonFormat(pattern = EXPIRES_FORMATTER)
	private LocalDateTime expires;

}
