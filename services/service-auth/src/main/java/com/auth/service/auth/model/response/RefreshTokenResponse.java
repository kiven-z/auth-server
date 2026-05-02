package com.auth.service.auth.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

	private static final String EXPIRES_FORMATTER = "yyyy/MM/dd HH:mm:ss";

	/**
	 * 访问令牌
	 */
	private String accessToken;

	/**
	 * 刷新令牌
	 */
	private String refreshToken;

	/**
	 * 阅读过期天数
	 */
	private Long readMeDay;

	/**
	 * 过期时间
	 */
	@JsonFormat(pattern = EXPIRES_FORMATTER)
	private LocalDateTime expires;

}
