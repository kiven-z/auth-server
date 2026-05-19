package com.auth.module.message.sender.template;

/**
 * FreeMarker template render failure.
 *
 * @author Bunny
 */
public class TemplateRenderException extends RuntimeException {

    public TemplateRenderException(String message) {
        super(message);
    }

    public TemplateRenderException(String message, Throwable cause) {
        super(message, cause);
    }

}
