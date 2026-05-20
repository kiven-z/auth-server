package com.auth.service.auth.authentication.application.command;

import com.auth.service.auth.authentication.domain.enums.AuthLoginLogType;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录用例命令基类：与 HTTP DTO 解耦，供应用层与认证策略使用。
 *
 * @author Bunny
 */
@Getter
@Setter
public abstract class AbstractLoginCommand {

	/**
	 * 是否记住登录（持久 Refresh Cookie）
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
