package com.auth.service.auth.authorization.application.invalidation.impact;

import com.auth.service.auth.authorization.domain.enums.AuthorizationChangeKind;
import com.auth.service.auth.authorization.domain.invalidation.model.RoleCodes;
import com.auth.service.auth.authorization.domain.invalidation.port.AuthorizationImpactQueryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * {@link ImpactResolverRegistry} 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImpactResolverRegistry 解析器注册表")
class ImpactResolverRegistryTest {

	@Mock
	private AuthorizationImpactQueryPort authorizationImpactQueryPort;

	@Test
	@DisplayName("按 RoleCodes 分发到角色解析器")
	void resolve_roleCodes_shouldReturnUserIds() {
		when(authorizationImpactQueryPort.findUserIdsByRoleCodes(List.of("ADMIN"))).thenReturn(Set.of(99L));

		RoleCodeImpactResolver roleResolver = new RoleCodeImpactResolver(authorizationImpactQueryPort);
		ImpactResolverRegistry registry = new ImpactResolverRegistry(List.of(roleResolver));

		Set<Long> userIds = registry.resolve(new RoleCodes(List.of("ADMIN")));

		assertEquals(Set.of(99L), userIds);
	}

	@Test
	@DisplayName("重复注册同一 kind 时构造失败")
	void constructor_duplicateKind_shouldThrow() {
		RoleCodeImpactResolver first = new RoleCodeImpactResolver(authorizationImpactQueryPort);
		RoleCodeImpactResolver second = new RoleCodeImpactResolver(authorizationImpactQueryPort);

		assertThrows(IllegalStateException.class, () -> new ImpactResolverRegistry(List.of(first, second)));
	}

}
