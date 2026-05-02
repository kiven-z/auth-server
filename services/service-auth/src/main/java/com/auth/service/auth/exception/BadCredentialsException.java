package com.auth.service.auth.exception;

/**
 * 用户名或密码错误异常
 *
 * @author Bunny
 */
public class BadCredentialsException extends RuntimeException {

	public BadCredentialsException(String message) {
		super(message);
	}

	public BadCredentialsException(String message, Throwable cause) {
		super(message, cause);
	}

}
