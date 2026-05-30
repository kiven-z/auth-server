package com.auth.common.web.resttemplate;

import com.auth.common.core.model.response.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FeignUtil} unit tests.
 */
class FeignUtilTest {

	@Test
	@DisplayName("isSuccess：Result.success() 无 data 时仍为 true（Void 场景）")
	void isSuccess_voidSuccessWithoutData_shouldBeTrue() {
		assertTrue(FeignUtil.isSuccess(Result.success()));
	}

	@Test
	@DisplayName("isSuccess：成功码且带 data 时为 true")
	void isSuccess_successWithData_shouldBeTrue() {
		assertTrue(FeignUtil.isSuccess(Result.success("x")));
	}

	@Test
	@DisplayName("isSuccess：null 或非成功码为 false")
	void isSuccess_nullOrError_shouldBeFalse() {
		assertFalse(FeignUtil.isSuccess(null));
		assertFalse(FeignUtil.isSuccess(Result.error("e")));
	}

	@Test
	@DisplayName("isSuccessWithData：成功码且带 data 时为 true")
	void isSuccessWithData_successWithData_shouldBeTrue() {
		assertTrue(FeignUtil.isSuccessWithData(Result.success("x")));
	}

	@Test
	@DisplayName("isSuccessWithData：成功但 data 为 null 时为 false")
	void isSuccessWithData_successWithoutData_shouldBeFalse() {
		assertFalse(FeignUtil.isSuccessWithData(Result.success()));
	}

	@Test
	@DisplayName("isSuccessWithData：null 或非成功码为 false")
	void isSuccessWithData_nullOrError_shouldBeFalse() {
		assertFalse(FeignUtil.isSuccessWithData(null));
		assertFalse(FeignUtil.isSuccessWithData(Result.error("e")));
	}

}
