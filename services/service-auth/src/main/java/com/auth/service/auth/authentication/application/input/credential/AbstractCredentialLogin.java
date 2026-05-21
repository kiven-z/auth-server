package com.auth.service.auth.authentication.application.input.credential;

import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 凭证类登录入站模型基类：用户名密码、邮箱、短信等；供 Controller 与认证策略共用。
 *
 * @author Bunny
 */
@Getter
@Setter
public abstract class AbstractCredentialLogin implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 是否记住登录（持久 Refresh Cookie；天数由服务端 Nacos refreshExpired 配置）
	 */
	private Boolean rememberMe;

	/**
	 * 当前登录方式对应的审计事件类型。
	 * @return 登录日志类型
	 */
	public abstract AuthLoginLogType loginLogType();

	/**
	 * 审计主体（用户名、邮箱或手机号等），用于尚未解析到用户时的日志。
	 * @return 主体字符串；无法解析时返回 null
	 */
	public abstract String principalForAudit();

}
