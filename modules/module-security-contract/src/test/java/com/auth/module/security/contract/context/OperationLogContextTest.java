package com.auth.module.security.contract.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link OperationLogContext} 单元测试
 *
 * @author Bunny
 */
class OperationLogContextTest {

	@Test
	@DisplayName("未设置时 getTargetId 应为 null")
	void getTargetId_shouldBeNullWhenUnset() {
		OperationLogContext.clear();
		assertNull(OperationLogContext.getTargetId());
	}

	@Test
	@DisplayName("setTargetId(null) 应等价于清除线程内值")
	void setTargetIdNull_shouldRemoveValue() {
		OperationLogContext.setTargetId(7L);
		OperationLogContext.setTargetId(null);
		assertNull(OperationLogContext.getTargetId());
	}

	@Test
	@DisplayName("setTargetId 后应能读取并在 clear 后恢复为 null")
	void setAndClear_shouldIsolatePerUsage() {
		OperationLogContext.clear();
		OperationLogContext.setTargetId(42L);
		assertEquals(42L, OperationLogContext.getTargetId());
		OperationLogContext.clear();
		assertNull(OperationLogContext.getTargetId());
	}

}
