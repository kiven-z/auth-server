package com.auth.module.security.contract.dto.invalidation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link InvalidationPayloadSupport} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InvalidationPayloadSupport 失效契约校验")
class InvalidationPayloadSupportTest {

	@Test
	@DisplayName("copyNonEmpty 应返回不可变拷贝")
	void copyNonEmpty_shouldReturnUnmodifiableCopy() {
		List<String> source = new java.util.ArrayList<>(List.of("ADMIN"));

		List<String> copied = InvalidationPayloadSupport.copyNonEmpty(source, "roleCodes");

		assertEquals(List.of("ADMIN"), copied);
		assertThrows(UnsupportedOperationException.class, () -> copied.add("X"));
	}

	@Test
	@DisplayName("copyNonEmpty 在 null 或空列表时应拒绝")
	void copyNonEmpty_whenNullOrEmpty_shouldThrow() {
		assertThrows(IllegalArgumentException.class, () -> InvalidationPayloadSupport.copyNonEmpty(null, "roleCodes"));
		assertThrows(IllegalArgumentException.class,
				() -> InvalidationPayloadSupport.copyNonEmpty(Collections.emptyList(), "roleCodes"));
	}

	@Test
	@DisplayName("requireNonNull 在 null 时应拒绝")
	void requireNonNull_whenNull_shouldThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> InvalidationPayloadSupport.requireNonNull(null, "subjectId"));
	}

}
