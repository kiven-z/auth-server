package com.auth.service.auth.api.dto.authentication.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

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
