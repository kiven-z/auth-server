package com.auth.service.system.common.exception;

import com.auth.common.core.model.response.Result;
import com.auth.service.system.common.exception.code.message.SystemMessageTransportResultCode;
import com.auth.service.system.message.infrastructure.exception.MessageSenderException;
import com.auth.service.system.message.infrastructure.modules.email.transport.EmailTransportException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link MessageTransportExceptionHandler} unit tests
 */
@DisplayName("MessageTransportExceptionHandler")
class MessageTransportExceptionHandlerTest {

	private final MessageTransportExceptionHandler handler = new MessageTransportExceptionHandler();

	@Test
	@DisplayName("EmailTransportException TO_EMPTY returns English message without i18n ext")
	void handleEmailTransport_validationReason() {
		EmailTransportException exception = new EmailTransportException(EmailTransportException.Reason.TO_EMPTY);

		ResponseEntity<Result<Object>> response = handler.handleEmailTransport(exception);

		assertEquals(400, response.getStatusCode().value());
		Result<Object> body = response.getBody();
		assertEquals(SystemMessageTransportResultCode.MESSAGE_TRANSPORT_VALIDATION_FAILED.getBizCode(), body.getCode());
		assertEquals("TO_EMPTY", body.getError());
		assertEquals("Recipient list is empty", body.getMessage());
		assertNull(body.getExt());
	}

	@Test
	@DisplayName("EmailTransportException SEND_FAILED returns 502 with English message")
	void handleEmailTransport_sendFailed() {
		EmailTransportException exception = new EmailTransportException(EmailTransportException.Reason.SEND_FAILED,
				"SMTP timeout");

		ResponseEntity<Result<Object>> response = handler.handleEmailTransport(exception);

		assertEquals(502, response.getStatusCode().value());
		Result<Object> body = response.getBody();
		assertEquals(SystemMessageTransportResultCode.MESSAGE_TRANSPORT_SEND_FAILED.getBizCode(), body.getCode());
		assertEquals("SEND_FAILED", body.getError());
		assertEquals("SMTP timeout", body.getMessage());
		assertNull(body.getExt());
	}

	@Test
	@DisplayName("MessageSenderException returns English message without i18n ext")
	void handleMessageSender_sendFailed() {
		MessageSenderException exception = new MessageSenderException("DINGTALK_WORK_NOTICE_FAILED",
				"DingTalk work notice failed: invalid agent");

		ResponseEntity<Result<Object>> response = handler.handleMessageSender(exception);

		assertEquals(502, response.getStatusCode().value());
		Result<Object> body = response.getBody();
		assertEquals("DINGTALK_WORK_NOTICE_FAILED", body.getError());
		assertEquals("DingTalk work notice failed: invalid agent", body.getMessage());
		assertNull(body.getExt());
	}

	@Test
	@DisplayName("MessageSenderException with BLANK error maps to validation 400")
	void handleMessageSender_validation() {
		MessageSenderException exception = new MessageSenderException("ALIYUN_DYPNS_PHONE_BLANK",
				"phone must not be blank");

		ResponseEntity<Result<Object>> response = handler.handleMessageSender(exception);

		assertEquals(400, response.getStatusCode().value());
		assertEquals(SystemMessageTransportResultCode.MESSAGE_TRANSPORT_VALIDATION_FAILED.getBizCode(),
				response.getBody().getCode());
	}

}
