package com.auth.common.web.exception;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.utils.ExceptionCauseUtil;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 验证 / 请求体相关异常处理 此处理器必须具有最高优先级，以避免被通用回退吞噬
 *
 * @author Bunny
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidationExceptionHandler extends BaseExceptionResponseBuilder {

	/**
	 * 处理方法参数验证异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Result<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
		// 获取绑定结果
		String details = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.sorted(Comparator.comparing(FieldError::getField))
			.map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"))
			.collect(Collectors.joining(", "));

		return warn(HttpStatus.BAD_REQUEST, 400, "Validation failed: " + details, exception, "Validation error");
	}

	/**
	 * 处理约束验证异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Result<Object>> handleConstraintViolation(ConstraintViolationException exception) {
		String details = exception.getConstraintViolations()
			.stream()
			.map(v -> v.getPropertyPath() + ": " + v.getMessage())
			.collect(Collectors.joining(", "));

		return warn(HttpStatus.BAD_REQUEST, 400, "Validation failed: " + details, exception, "Constraint violation");
	}

	/**
	 * 处理HTTP消息不可读异常
	 * @param exception 异常
	 * @return 响应实体
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Result<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
		Throwable root = ExceptionCauseUtil.rootCause(exception);
		String rootName = root != null ? root.getClass().getSimpleName() : "Unknown";

		String message;
		// 根据根类型设置消息
		switch (rootName) {
			case "JsonParseException" -> message = "Malformed JSON: please check syntax.";
			case "InvalidFormatException" -> message = "Invalid JSON value format: please check field types.";
			case "MismatchedInputException" -> message = "JSON does not match the expected schema.";
			default -> message = "Request body is not readable JSON.";
		}

		return warn(HttpStatus.BAD_REQUEST, 400, message, exception, "Unreadable HTTP message");
	}

}
