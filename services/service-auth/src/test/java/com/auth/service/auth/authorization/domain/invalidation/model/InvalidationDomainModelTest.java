package com.auth.service.auth.authorization.domain.invalidation.model;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.RoleInvalidatePayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 失效领域命令校验测试
 */
@DisplayName("InvalidationCommand 领域命令")
class InvalidationDomainModelTest {

	@Test
	@DisplayName("InvalidationCommand 拒绝空白 eventId")
	void invalidationCommand_blankEventId_shouldThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> new InvalidationCommand("  ", new RoleInvalidatePayload(List.of("ADMIN"))));
	}

	@Test
	@DisplayName("InvalidationCommand 拒绝 null payload")
	void invalidationCommand_nullPayload_shouldThrow() {
		assertThrows(IllegalArgumentException.class, () -> new InvalidationCommand("evt-1", null));
	}

	@Test
	@DisplayName("InvalidationCommand 正常构造")
	void invalidationCommand_valid_shouldHoldPayload() {
		RoleInvalidatePayload payload = new RoleInvalidatePayload(List.of("CUSTOM_R_ONE"));
		InvalidationCommand command = new InvalidationCommand("evt-1", payload);

		assertEquals("evt-1", command.eventId());
		assertEquals(payload, command.payload());
		assertEquals(AuthorizationChangeKind.ROLE, command.payload().kind());
	}

}
