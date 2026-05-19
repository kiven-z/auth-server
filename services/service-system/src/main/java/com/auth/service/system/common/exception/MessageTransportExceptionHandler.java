package com.auth.service.system.common.exception;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.common.exception.code.message.SystemMessageTransportResultCode;
import com.auth.service.system.message.infrastructure.exception.MessageSenderException;
import com.auth.service.system.message.infrastructure.modules.email.transport.EmailTransportException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Message transport-layer exceptions (English-only API response, no i18n).
 *
 * @author Bunny
 */
@Slf4j
@Order(8)
@RestControllerAdvice
public class MessageTransportExceptionHandler {

	/**
	 * Maps sender error code to transport result code.
	 * @param exception sender exception
	 * @return transport result code
	 */
	private static SystemMessageTransportResultCode resolveResultCode(MessageSenderException exception) {
		String error = exception.getError();
		if (error != null && (error.contains("BLANK") || error.contains("EMPTY"))) {
			return SystemMessageTransportResultCode.MESSAGE_TRANSPORT_VALIDATION_FAILED;
		}
		return SystemMessageTransportResultCode.MESSAGE_TRANSPORT_SEND_FAILED;
	}

	/**
	 * Handles email MIME transport failures.
	 * @param exception transport exception
	 * @return error response
	 */
	@ExceptionHandler(EmailTransportException.class)
	public ResponseEntity<Result<Object>> handleEmailTransport(EmailTransportException exception) {
		log.warn("Email transport exception: reason={}, message={}", exception.getReason(), exception.getMessage(),
				exception);

		EmailTransportException.Reason reason = exception.getReason();
		SystemMessageTransportResultCode resultCode = reason.isValidationFailure()
				? SystemMessageTransportResultCode.MESSAGE_TRANSPORT_VALIDATION_FAILED
				: SystemMessageTransportResultCode.MESSAGE_TRANSPORT_SEND_FAILED;

		Result<Object> body = SystemResultResponseFactory.buildEnglish(resultCode, exception.getMessage(),
				reason.name());
		HttpStatus status = SystemResultResponseFactory.resolveHttpStatus(resultCode);
		return ResponseEntity.status(status).body(body);
	}

	/**
	 * Handles DingTalk / Dypns infrastructure failures.
	 * @param exception sender exception
	 * @return error response
	 */
	@ExceptionHandler(MessageSenderException.class)
	public ResponseEntity<Result<Object>> handleMessageSender(MessageSenderException exception) {
		log.warn("Message sender exception: error={}, message={}", exception.getError(), exception.getMessage(),
				exception);

		SystemMessageTransportResultCode resultCode = resolveResultCode(exception);
		Result<Object> body = SystemResultResponseFactory.buildEnglish(resultCode, exception.getMessage(),
				exception.getError());
		HttpStatus status = SystemResultResponseFactory.resolveHttpStatus(resultCode);
		return ResponseEntity.status(status).body(body);
	}

}
