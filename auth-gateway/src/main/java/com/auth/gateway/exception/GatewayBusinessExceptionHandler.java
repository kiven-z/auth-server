package com.auth.gateway.exception;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.exception.ValidationExceptionHandler;
import com.auth.gateway.support.GatewayResultFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 为 Controller / Router 预留；当前网关无 Controller，Filter 链错误由
 *
 * @author Bunny
 */
@Slf4j
@Order(10)
@RestControllerAdvice
public class GatewayBusinessExceptionHandler extends ValidationExceptionHandler {

	private final MessageSource messageSource;

	public GatewayBusinessExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * 处理 Controller / Router 场景
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(GatewayBusinessException.class)
	public ResponseEntity<Result<Object>> exceptionHandler(GatewayBusinessException exception) {
		log.warn("Gateway business exception: {}", exception.toString(), exception);

		HttpStatus status = GatewayResultFactory.resolveHttpStatus(exception.getResultCode());

		Result<Object> body = GatewayResultFactory.build(messageSource, exception);
		return ResponseEntity.status(status).body(body);
	}

}
