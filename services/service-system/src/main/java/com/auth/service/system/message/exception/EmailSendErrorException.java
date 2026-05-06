package com.auth.service.system.message.exception;

/**
 * 邮件发送异常
 *
 * @author Bunny
 */
public class EmailSendErrorException extends RuntimeException {

	public EmailSendErrorException(String message) {
		super(message);
	}

	public EmailSendErrorException() {
		super("邮件发送失败");
	}

}
