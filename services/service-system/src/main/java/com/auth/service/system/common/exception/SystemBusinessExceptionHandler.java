package com.auth.service.system.common.exception;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.exception.ValidationExceptionHandler;
import com.auth.service.system.message.infrastructure.exception.SystemMessageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 系统业务与消息发送异常处理器（与 {@code AuthExceptionHandler} 一致：{@link MessageSource} +
 * {@link SystemResultResponseFactory}）
 *
 * @author Bunny
 */
@Slf4j
@Order(10)
@RestControllerAdvice
public class SystemBusinessExceptionHandler extends ValidationExceptionHandler {

	private final MessageSource messageSource;

	public SystemBusinessExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * 处理系统业务异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(SystemBusinessException.class)
	public ResponseEntity<Result<Object>> handleSystemBusiness(SystemBusinessException exception) {
		log.warn("System business exception: {}", exception.toString(), exception);

		var rc = exception.getResultCode();
		Result<Object> body = SystemResultResponseFactory.build(messageSource, exception);
		HttpStatus status = SystemResultResponseFactory.resolveHttpStatus(rc);
		return ResponseEntity.status(status).body(body);
	}

	/**
	 * 处理消息发送域异常（传输层 / 渠道基础设施）
	 * @param exception 消息异常
	 * @return 响应实体
	 */
	@ExceptionHandler(SystemMessageException.class)
	public ResponseEntity<Result<Object>> handleSystemMessage(SystemMessageException exception) {
		log.warn("System message exception: {}", exception.toString(), exception);

		var rc = exception.getResultCode();
		Result<Object> body = SystemResultResponseFactory.build(messageSource, exception);
		HttpStatus status = SystemResultResponseFactory.resolveHttpStatus(rc);
		return ResponseEntity.status(status).body(body);
	}

}
