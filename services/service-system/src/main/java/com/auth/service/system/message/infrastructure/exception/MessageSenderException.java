package com.auth.service.system.message.infrastructure.exception;

import lombok.Getter;

/**
 * Message channel infrastructure failure (DingTalk, Dypns, etc.).
 * <p>
 * English-only runtime message; no i18n in sender module.
 * </p>
 *
 * @author Bunny
 */
@Getter
public class MessageSenderException extends RuntimeException {

	private final String error;

	public MessageSenderException(String error, String message) {
		super(message);
		this.error = error;
	}

	public MessageSenderException(String error, String message, Throwable cause) {
		super(message, cause);
		this.error = error;
	}

}
