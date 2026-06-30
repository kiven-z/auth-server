package com.auth.service.auth.model.value.login;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 刷新令牌完成后的应用读模型。
 *
 * @author Bunny
 */
@Getter
@Setter
public class CompletedRefreshSession {

	private String accessToken;

	private String refreshToken;

	private LocalDateTime expires;

	private Long readMeDay;

}
