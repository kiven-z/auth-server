package com.auth.service.system.common.exception;

/**
 * 模板渲染失败
 *
 * @author Bunny
 */
public class TemplateRenderException extends RuntimeException {

	public TemplateRenderException(String message, Throwable cause) {
		super(message, cause);
	}

}
