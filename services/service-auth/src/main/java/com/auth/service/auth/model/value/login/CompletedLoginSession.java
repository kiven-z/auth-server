package com.auth.service.auth.model.value.login;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录会话签发完成后的应用读模型
 *
 * @author Bunny
 */
@Getter
@Setter
public class CompletedLoginSession {

	private Long id;

	private String username;

	private List<String> roles;

	private List<String> permissions;

	private String accessToken;

	private String refreshToken;

	private LocalDateTime expires;

	private Long readMeDay;

}
