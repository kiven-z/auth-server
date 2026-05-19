package com.auth.service.system.common.exception;

/**
 * FreeMarker template render failure.
 *
 * @author Bunny
 */
public class TemplateRenderException extends RuntimeException {

	public TemplateRenderException(String message, Throwable cause) {
		super(message, cause);
	}

}
