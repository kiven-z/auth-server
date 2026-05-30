package com.auth.common.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.auth.common.core.model.response.Result;

/**
 * {@link ValidationExceptionHandler} 单元测试
 */
class ValidationExceptionHandlerTest {

	private ValidationExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new ValidationExceptionHandler();
	}

	@DisplayName("BindException：类型转换失败应返回 invalid value format")
	@Test
	void handleBindException_typeMismatchLong_returnsInvalidValueFormatMessage() {
		BindException exception = bindExceptionWithTypeMismatch("abc");

		ResponseEntity<Result<Object>> response = handler.handleBindException(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(400, response.getBody().getCode());
		assertEquals("Validation failed: jobId: invalid value format", response.getBody().getMessage());
	}

	@DisplayName("MethodArgumentNotValidException：由 BindException 统一处理")
	@Test
	void handleBindException_methodArgumentNotValid_returnsInvalidValueFormatMessage() throws NoSuchMethodException {
		DummyQuery target = new DummyQuery();
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "logJobQuery");
		bindingResult.addError(new FieldError("logJobQuery", "jobId", "xyz", false,
				new String[] { "typeMismatch.jobId", "typeMismatch.java.lang.Long", "typeMismatch" },
				new Object[] { "jobId", Long.class }, "Failed to convert"));
		Method endpoint = ValidationExceptionHandlerTest.class.getDeclaredMethod("dummyEndpoint", DummyQuery.class);
		MethodParameter parameter = new MethodParameter(endpoint, 0);
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

		ResponseEntity<Result<Object>> response = handler.handleBindException(exception);

		assertEquals("Validation failed: jobId: invalid value format", response.getBody().getMessage());
	}

	@DisplayName("MethodArgumentTypeMismatchException：单个参数类型错误应返回 invalid value format")
	@Test
	void handleMethodArgumentTypeMismatch_longParam_returnsInvalidValueFormatMessage() {
		MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException("abc", Long.class,
				"jobId", null, null);

		ResponseEntity<Result<Object>> response = handler.handleMethodArgumentTypeMismatch(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Validation failed: jobId: invalid value format", response.getBody().getMessage());
	}

	@DisplayName("BindException：普通校验消息应原样输出英文字段提示")
	@Test
	void handleBindException_constraintMessage_preservesDefaultMessage() {
		DummyQuery target = new DummyQuery();
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "form");
		bindingResult.addError(new FieldError("form", "name", null, false, new String[] { "NotBlank.form.name" }, null,
				"must not be blank"));
		BindException exception = new BindException(bindingResult);

		ResponseEntity<Result<Object>> response = handler.handleBindException(exception);

		assertEquals("Validation failed: name: must not be blank", response.getBody().getMessage());
	}

	@SuppressWarnings("unused")
	private void dummyEndpoint(DummyQuery query) {
		// 仅用于构造 MethodArgumentNotValidException 测试
	}

	private BindException bindExceptionWithTypeMismatch(String rejectedValue) {
		DummyQuery target = new DummyQuery();
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "logJobQuery");
		bindingResult.addError(new FieldError(
				"logJobQuery", "jobId", rejectedValue, false, new String[] { "typeMismatch.logJobQuery.jobId",
						"typeMismatch.jobId", "typeMismatch.java.lang.Long", "typeMismatch" },
				new Object[] { "jobId", Long.class }, "Failed to convert"));
		return new BindException(bindingResult);
	}

	/**
	 * 测试用查询对象
	 */
	private static class DummyQuery {

		private Long jobId;

		private String name;

		void setJobId(Long jobId) {
			this.jobId = jobId;
		}

		void setName(String name) {
			this.name = name;
		}

	}

}
