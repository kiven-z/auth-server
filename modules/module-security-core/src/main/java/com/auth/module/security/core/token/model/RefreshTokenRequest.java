package com.auth.module.security.core.token.model;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 刷新令牌请求
 *
 * @author Bunny
 */
@Getter
@Setter
public class RefreshTokenRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 刷新令牌
	 */
	@NotBlank(message = "刷新令牌不能为空")
	private String refreshToken;

}