package com.auth.service.auth.authorization.domain.invalidation.model;

import com.auth.service.auth.authorization.domain.enums.AuthorizationChangeKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 失效领域模型校验测试
 */
@DisplayName("授权失效领域模型")
class InvalidationDomainModelTest {

	@Test
	@DisplayName("RoleCodes 拒绝空列表")
	void roleCodes_empty_shouldThrow() {
		assertThrows(IllegalArgumentException.class, () -> new RoleCodes(List.of()));
	}

	@Test
	@DisplayName("InvalidationCommand 拒绝空白 eventId")
	void invalidationCommand_blankEventId_shouldThrow() {
		assertThrows(IllegalArgumentException.class,
				() -> new InvalidationCommand(new RoleCodes(List.of("ADMIN")), "  "));
	}

	@Test
	@DisplayName("RoleCodes 绑定 ROLE 维度")
	void roleCodes_kind_shouldBeRole() {
		RoleCodes target = new RoleCodes(List.of("CUSTOM_R_ONE"));

		assertEquals(AuthorizationChangeKind.ROLE, target.kind());
	}

}
