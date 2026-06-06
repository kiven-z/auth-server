package com.auth.service.system.admin.excel.export;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.exception.ExportException;

import lombok.extern.slf4j.Slf4j;

/**
 * 导出异常处理
 *
 * @author Bunny
 */
@Slf4j
@Order(9)
@RestControllerAdvice
public class ExportExceptionHandler {

	private static final int EXPORT_VALIDATION_BIZ_CODE = 400;

	private static final String EXPORT_VALIDATION_ERROR = "EXPORT_VALIDATION_FAILED";

	/**
	 * 处理导出异常
	 * @param exception 导出异常
	 * @return 响应实体
	 */
	@ExceptionHandler(ExportException.class)
	public ResponseEntity<Result<Object>> handleExportException(ExportException exception) {
		log.warn("Export validation failed: {}", exception.getMessage());
		Result<Object> body = Result.error(EXPORT_VALIDATION_BIZ_CODE, EXPORT_VALIDATION_ERROR, exception.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

}
