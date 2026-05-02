package com.auth.common.jwt.exception;

/**
 * 与历史全局异常处理器兼容的解析失败类型
 *
 * @author Bunny
 */
public class JwtParseException extends JwtException {

	public JwtParseException(String message, Throwable cause) {
		super("JWT_PARSE_ERROR", message, cause);
	}

}
