package com.auth.module.message.sender.modules.email.transport;

import lombok.Getter;

/**
 * Email transport layer failure (validation, attachment resolve, MIME send).
 *
 * @author Bunny
 */
@Getter
public class EmailTransportException extends RuntimeException {

    private final Reason reason;

    public EmailTransportException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public EmailTransportException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    /**
     * Transport failure reason.
     */
    public enum Reason {
        SEND_DTO_NULL, TO_EMPTY, SUBJECT_EMPTY, BODY_EMPTY, ATTACHMENT_FILENAME_EMPTY, ATTACHMENT_BASE64_FAILED,
        ATTACHMENT_HTTP_STATUS, ATTACHMENT_DOWNLOAD_FAILED, SEND_FAILED
    }

}
